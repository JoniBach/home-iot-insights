package core.usecases.calculate.weekly.average

import core.domain.context.HumidityContextProvider
import core.domain.{InsightCalculator, InsightDataAggregator, InsightType}

import core.domain.time.TimeProvider
import core.entities.Insight
import core.ports.InsightsPort

import java.time.Instant
import java.util.UUID
import cats.Monad
import cats.syntax.all._

/**
 * Use case for calculating weekly average humidity.
 * This is designed to be run weekly (e.g., at 1 AM) to process the previous week's data.
 */
final class CalculateWeeklyAverageHumidity[F[_]: Monad](
    timeProvider: TimeProvider[F],
    dataAggregator: InsightDataAggregator[F],
    contextProvider: HumidityContextProvider[F],
    insightsPort: InsightsPort[F]
) {
  // Weekly Average Humidity insight type ID
  private val insightTypeId = UUID.fromString("c160c68c-0b82-4e1a-8bd8-6aab738c0266") // Matches HumidityAverage in InsightType

  /**
   * Executes the weekly average humidity calculation.
   * Processes data from midnight to midnight of the previous week.
   * 
   * @return A list of created insights with average humidity for each device and sensor
   */
  def execute(): F[List[Insight]] = {
    for {
      // Get the time range for the previous week
      (start, end) <- timeProvider.getPreviousWeekRange
      now <- timeProvider.now
      
      // Get average humidity for all device-sensor pairs
      averages <- dataAggregator.aggregateWeeklyInsight(start, end)
      
      // Get context for all devices and sensors with readings
      context <- contextProvider.getContext(
        deviceIds = averages.keySet.map(_._1),
        sensorKeys = averages.keySet.map(_._2)
      )
      
      // Create and save insights
      insights = averages.map { case ((deviceId, sensorKey), avgHumidity) =>
        val relationship = context.deviceRelationships.get(deviceId)
        
        Insight(
          id = None,
          macAddress = deviceId,
          sensor = sensorKey,
          value = avgHumidity,
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

object CalculateWeeklyAverageHumidity {
  /**
   * Creates a new instance with default implementations of all dependencies.
   */
  def default[F[_]: Monad](
    readingsPort: core.ports.ReadingsPort[F],
    deviceRoomBuildingsPort: core.ports.DeviceRoomBuildingsPort[F],
    sensorsPort: core.ports.SensorsPort[F],
    insightsPort: core.ports.InsightsPort[F]
  ): CalculateWeeklyAverageHumidity[F] = {
    val timeProvider = TimeProvider.default[F]
    val insightCalculator = InsightCalculator.default[F]
    val dataAggregator = InsightDataAggregator.default[F](readingsPort, insightCalculator, InsightType.Humidity)
    val contextProvider = HumidityContextProvider.default[F](deviceRoomBuildingsPort, sensorsPort)
    
    new CalculateWeeklyAverageHumidity[F](
      timeProvider = timeProvider,
      dataAggregator = dataAggregator,
      contextProvider = contextProvider,
      insightsPort = insightsPort
    )
  }
}
