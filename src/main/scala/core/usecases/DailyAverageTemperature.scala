package application.usecases

import core.entities.{Insight, Reading}
import core.ports.{DeviceRoomBuildingRepository, ReadingRepository, SensorRepository}

import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.util.UUID
import cats.Monad
import cats.syntax.all._

final class DailyAverageTemperature[F[_]: Monad](
    readingRepo: ReadingRepository[F],
    deviceRoomBuildingRepo: DeviceRoomBuildingRepository[F],
    sensorRepo: SensorRepository[F]
) {


  private def getMidnight(daysAgo: Int): Instant = LocalDateTime.now
    .minusDays(daysAgo)
    .withHour(0)
    .withMinute(0)
    .withSecond(0)
    .withNano(0)
    .toInstant(ZoneOffset.UTC)

  private def calculateAverage(readings: List[Reading]): Double = {
    if (readings.nonEmpty)
      BigDecimal(readings.map(_.temperature).sum / readings.length)
        .setScale(2, BigDecimal.RoundingMode.HALF_UP)
        .toDouble
    else 0.0
  }

  def execute(): F[List[Insight]] = {
    val yesterdayMidnight = getMidnight(1)
    val todayMidnight = getMidnight(0)
    val now = Instant.now()
    val insightTypeId = UUID.fromString("c160c68c-0b82-4e1a-8bd8-6aab738c0266")

    readingRepo.getReadingsForPeriod(yesterdayMidnight, todayMidnight).flatMap { readings =>
      // Get unique device IDs from readings
      val deviceIds = readings.map(_.macAddress).distinct
      
      // Get unique sensor keys from readings
      val sensorKeys = readings.map(_.sensor).distinct
      
      // Fetch all device-room-building relationships for devices with readings
      deviceIds.traverse(deviceId => 
        deviceRoomBuildingRepo.findByDeviceId(deviceId).map(_.map(deviceId -> _))
      ).flatMap { deviceRelationships =>
        val deviceRelationshipsMap = deviceRelationships.flatten.toMap

        // Fetch sensor IDs for all unique sensor keys
        sensorKeys.traverse { sensorKey =>
          sensorRepo.findByKey(sensorKey).map(_.map(sensor => sensorKey -> sensor.id))
        }.map { sensorIds =>
          val sensorIdsMap = sensorIds.flatten.toMap

          // Group readings by device (macAddress) and calculate average for each
          val insights = readings
            .groupBy(r => (r.macAddress, r.sensor))
            .flatMap { case ((deviceId, sensorKey), deviceReadings) =>
              val avgTemperature = calculateAverage(deviceReadings)

              // Get the relationship for this device, if it exists
              val relationship = deviceRelationshipsMap.get(deviceId)

              // Get the sensor ID for this sensor key, if it exists
              val sensorId = sensorIdsMap.get(sensorKey)

              // Create an insight for this device and sensor combination
              Some(Insight(
                id = None,
                buildingId = relationship.flatMap(_.buildingId),
                roomId = relationship.flatMap(_.roomId),
                sensorId = sensorId,
                insightTypeId = Some(insightTypeId),
                deviceId = Some(deviceId),
                createdAt = now,
                rangeFrom = Some(yesterdayMidnight),
                rangeTo = Some(todayMidnight),
                value = avgTemperature
              ))
            }
            .toList

          insights
        }
      }
    }
  }
}
