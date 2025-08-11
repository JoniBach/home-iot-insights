package core.usecases.calculate.daily.average

import core.domain.context.HumidityContextProvider
import core.domain.humidity.{HumidityCalculator, HumidityDataAggregator}
import core.domain.time.TimeProvider
import core.entities.Insight
import core.ports.InsightsPort

import java.time.Instant
import java.util.UUID
import cats.Monad
import cats.syntax.all._

/**
 * Use case for calculating daily average humidity.
 * This is designed to be run daily (e.g., at 1 AM) to process the previous day's data.
 */
final class CalculateDailyAverageHumidity[F[_]: Monad](
    timeProvider: TimeProvider[F],
    dataAggregator: HumidityDataAggregator[F],
    contextProvider: HumidityContextProvider[F],
    insightsPort: InsightsPort[F]
) {
  // Daily Average Humidity insight type ID
  private val insightTypeId = UUID.fromString("c160c68c-0b82-4e1a-8bd8-6aab738c0266") // Matches HumidityAverage in InsightType

  /**
   * Executes the daily average humidity calculation.
   * Processes data from midnight to midnight of the previous day.
   * 
   * @return A list of created insights with average humidity for each device and sensor
   */
  def execute(): F[List[Insight]] = {
    for {
      // Get the time range for the previous day
      (start, end) <- timeProvider.getPreviousDayRange
      now <- timeProvider.now
      
      // Get average humidity for all device-sensor pairs
      averages <- dataAggregator.aggregateDailyHumidity(start, end)
      
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

object CalculateDailyAverageHumidity {
  /**
   * Creates a new instance with default implementations of all dependencies.
   */
  def default[F[_]: Monad](
    readingsPort: core.ports.ReadingsPort[F],
    deviceRoomBuildingsPort: core.ports.DeviceRoomBuildingsPort[F],
    sensorsPort: core.ports.SensorsPort[F],
    insightsPort: core.ports.InsightsPort[F]
  ): CalculateDailyAverageHumidity[F] = {
    val timeProvider = TimeProvider.default[F]
    val humidityCalculator = HumidityCalculator.default[F]
    val dataAggregator = HumidityDataAggregator.default[F](readingsPort, humidityCalculator)
    val contextProvider = HumidityContextProvider.default[F](deviceRoomBuildingsPort, sensorsPort)
    
    new CalculateDailyAverageHumidity[F](
      timeProvider = timeProvider,
      dataAggregator = dataAggregator,
      contextProvider = contextProvider,
      insightsPort = insightsPort
    )
  }
}
