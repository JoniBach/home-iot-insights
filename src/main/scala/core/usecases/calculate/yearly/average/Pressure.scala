package core.usecases.calculate.yearly.average

import core.domain.context.PressureContextProvider
import core.domain.{InsightCalculator, InsightDataAggregator, InsightType}
import core.domain.time.TimeProvider
import core.entities.Insight
import core.ports.InsightsPort
import core.ports.ReadingsPort
import core.ports.DeviceRoomBuildingsPort
import core.ports.SensorsPort
import core.ports.InsightsPort

import java.time.Instant
import java.util.UUID
import cats.Monad
import cats.syntax.all._

/** Use case for calculating yearly average pressure. This is designed to be run
  * yearly (e.g., at 1 AM) to process the previous year's data.
  */
final class CalculateYearlyAveragePressure[F[_]: Monad](
    timeProvider: TimeProvider[F],
    dataAggregator: InsightDataAggregator[F],
    contextProvider: PressureContextProvider[F],
    insightsPort: InsightsPort[F]
) {
  // Yearly Average Pressure insight type ID
  private val insightTypeId = UUID.fromString(
    "c160c68c-0b82-4e1a-8bd8-6aab738c0266"
  ) // Matches PressureAverage in InsightType

  /** Executes the yearly average pressure calculation. Processes data from
    * midnight to midnight of the previous year.
    *
    * @return
    *   A list of created insights with average pressure for each device and
    *   sensor
    */
  def execute(): F[List[Insight]] = {
    for {
      // Get the time range for the previous year
      (start, end) <- timeProvider.getPreviousYearRange
      now <- timeProvider.now

      // Get average pressure for all device-sensor pairs
      averages <- dataAggregator.aggregateYearlyInsight(start, end)

      // Get context for all devices and sensors with readings
      context <- contextProvider.getContext(
        deviceIds = averages.keySet.map(_._1),
        sensorKeys = averages.keySet.map(_._2)
      )

      // Create and save insights
      insights = averages.map { case ((deviceId, sensorKey), avgPressure) =>
        val relationship = context.deviceRelationships.get(deviceId)

        Insight(
          id = None,
          macAddress = deviceId,
          sensor = sensorKey,
          value = avgPressure,
          buildingId = relationship.flatMap(_.buildingId),
          roomId = relationship.flatMap(_.roomId),
          insightTypeId = Some(insightTypeId),
          rangeFrom = Some(start),
          rangeTo = Some(end),
          createdAt = now
        )
      }.toList

      // Save all insights
      savedInsights <- insights.traverse(insightsPort.create)
    } yield savedInsights
  }
}

object CalculateYearlyAveragePressure {
  def default[F[_]: Monad](
      readingsPort: ReadingsPort[F],
      deviceRoomBuildingsPort: DeviceRoomBuildingsPort[F],
      sensorsPort: SensorsPort[F],
      insightsPort: InsightsPort[F]
  ): CalculateYearlyAveragePressure[F] = {
    val timeProvider = TimeProvider.default[F]
    val insightCalculator = InsightCalculator.default[F]
    val dataAggregator =
      InsightDataAggregator.default[F](readingsPort, insightCalculator, InsightType.Pressure)
    val contextProvider =
      PressureContextProvider.default[F](deviceRoomBuildingsPort, sensorsPort)

    new CalculateYearlyAveragePressure[F](
      timeProvider = timeProvider,
      dataAggregator = dataAggregator,
      contextProvider = contextProvider,
      insightsPort = insightsPort
    )
  }
}
