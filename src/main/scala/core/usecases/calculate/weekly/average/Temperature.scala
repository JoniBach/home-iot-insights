package core.usecases.calculate.weekly.average

import core.domain.context.{TemperatureContext, TemperatureContextProvider}
import core.domain.{InsightCalculator, InsightDataAggregator, InsightType}
import core.domain.time.TimeProvider
import core.entities.Insight
import core.ports.InsightsPort

import java.time.Instant
import java.util.UUID
import cats.Monad
import cats.syntax.all._

/**
 * Use case for calculating weekly average temperatures.
 * This is designed to be run weekly (e.g., at 1 AM) to process the previous day's data.
 */
final class CalculateWeeklyAverageTemperature[F[_]: Monad](
    timeProvider: TimeProvider[F],
    dataAggregator: InsightDataAggregator[F],
    contextProvider: TemperatureContextProvider[F],
    insightsPort: InsightsPort[F]
) {
  // Weekly Average Temperature insight type ID
  private val insightTypeId = UUID.fromString("56da8d03-a64e-4395-8a7c-ebedbf8b122b")

  /**
   * Executes the weekly average temperature calculation.
   * Processes data from midnight to midnight of the previous week.
   * 
   * @return A list of created insights with average temperatures for each device and sensor
   */
  def execute(): F[List[Insight]] = {
    for {
      // Get the time range for the previous week
      (start, end) <- timeProvider.getPreviousWeekRange
      now <- timeProvider.now
      
      // Get average temperatures for all device-sensor pairs
      averages <- dataAggregator.aggregateWeeklyInsight(start, end)
      
      // Get context for all devices and sensors with readings
      context <- contextProvider.getContext(
        deviceIds = averages.keySet.map(_._1),
        sensorKeys = averages.keySet.map(_._2)
      )
      
      // Create and save insights
      insights = averages.map { case ((deviceId, sensorKey), avgTemp) =>
        val relationship = context.deviceRelationships.get(deviceId)
        
        Insight(
          id = None,
          macAddress = deviceId,
          sensor = sensorKey,
          value = avgTemp,
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

object CalculateWeeklyAverageTemperature {
  /**
   * Creates a new instance with default implementations of all dependencies.
   */
  def default[F[_]: Monad](
    readingsPort: core.ports.ReadingsPort[F],
    deviceRoomBuildingsPort: core.ports.DeviceRoomBuildingsPort[F],
    sensorsPort: core.ports.SensorsPort[F],
    insightsPort: core.ports.InsightsPort[F]
  ): CalculateWeeklyAverageTemperature[F] = {
    val timeProvider = TimeProvider.default[F]
    val insightCalculator = InsightCalculator.default[F]
    val dataAggregator = InsightDataAggregator.default[F](readingsPort, insightCalculator, InsightType.Temperature)
    val contextProvider = TemperatureContextProvider.default[F](deviceRoomBuildingsPort, sensorsPort)
    
    new CalculateWeeklyAverageTemperature[F](
      timeProvider = timeProvider,
      dataAggregator = dataAggregator,
      contextProvider = contextProvider,
      insightsPort = insightsPort
    )
  }
}
