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

  /**
   * Executes the daily average temperature calculation.
   * Processes data from midnight to midnight of the previous day.
   * This is designed to be run at 1 AM each day.
   * 
   * @return A list of insights with average temperatures for each device and sensor
   */
  def execute(): F[List[Insight]] = {
    // Get timestamps for the previous day (midnight to midnight)
    val previousDayStart = getMidnight(1)  // Yesterday at 00:00:00
    val previousDayEnd = getMidnight(0)    // Today at 00:00:00
    val now = Instant.now()
    val insightTypeId = UUID.fromString("c160c68c-0b82-4e1a-8bd8-6aab738c0266") // Daily Average Temperature type

    // Get all readings for the previous day (midnight to midnight)
    readingRepo.getReadingsForPeriod(previousDayStart, previousDayEnd).flatMap { readings =>
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
                macAddress = deviceId,
                sensor = sensorKey,
                value = avgTemperature,
                buildingId = relationship.flatMap(_.buildingId),
                roomId = relationship.flatMap(_.roomId),
                insightTypeId = Some(insightTypeId),
                rangeFrom = Some(previousDayStart),
                rangeTo = Some(previousDayEnd),
                createdAt = now
              ))
            }
            .toList

          insights
        }
      }
    }
  }
}
