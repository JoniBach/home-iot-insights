package core.usecases

import core.entities.{Insight, InsightType, Reading}
import core.ports.ReadingsPort

import cats.Monad
import cats.syntax.all._
import java.time.Instant

final class GenerateInsights[F[_]: Monad](
    readingsPort: ReadingsPort[F]
) {

  /**
   * Generates insights for both temperature and humidity from the latest readings
   */
  def execute(): F[List[Insight]] =
    readingsPort.getLatestReadings(limit = 10).map { readings =>
      // Generate insights for each reading
      readings.flatMap { reading =>
        val timestamp = Instant.now()
        
        // Create both temperature and humidity insights for each reading
        List(
          // Temperature insight
          createInsight(
            reading = reading,
            value = reading.temperature,
            insightType = InsightType.TemperatureCurrent,
            timestamp = timestamp
          ),
          // Humidity insight
          createInsight(
            reading = reading,
            value = reading.humidity,
            insightType = InsightType.HumidityCurrent,
            timestamp = timestamp
          )
        )
      }
    }
    
  /**
   * Helper method to create an Insight from a Reading
   */
  private def createInsight(
    reading: Reading,
    value: Double,
    insightType: InsightType,
    timestamp: Instant
  ): Insight = {
    Insight(
      id = None,
      macAddress = reading.macAddress,
      sensor = reading.sensor,
      value = value,
      buildingId = None,  // These would be populated from context in a real implementation
      roomId = None,      // These would be populated from context in a real implementation
      insightTypeId = Some(insightType.id),
      rangeFrom = Some(timestamp.minusSeconds(300)),  // Last 5 minutes
      rangeTo = Some(timestamp),
      createdAt = timestamp
    )
  }
  
  /**
   * Calculate average humidity over a time period (example method)
   */
  def calculateAverageHumidity(readings: List[Reading]): Option[Double] = {
    if (readings.isEmpty) None
    else Some(readings.map(_.humidity).sum / readings.size)
  }
  
  /**
   * Calculate average temperature over a time period (example method)
   */
  def calculateAverageTemperature(readings: List[Reading]): Option[Double] = {
    if (readings.isEmpty) None
    else Some(readings.map(_.temperature).sum / readings.size)
  }
}
