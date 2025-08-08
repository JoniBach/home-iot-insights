package core.domain.temperature

import core.entities.Reading
import core.ports.ReadingsPort
import cats.Monad
import cats.syntax.all._
import java.time.Instant

/**
 * Aggregates temperature data for specific time periods.
 */
trait TemperatureDataAggregator[F[_]] {
  /**
   * Aggregates daily temperature data for the specified time range.
   * @return Map of (deviceId, sensorKey) to average temperature
   */
  def aggregateDailyTemperatures(
    start: Instant,
    end: Instant
  ): F[Map[(String, String), Double]]  // (deviceId, sensorKey) -> avgTemp
}

object TemperatureDataAggregator {
  
  def default[F[_]: Monad](
    readingsPort: ReadingsPort[F],
    temperatureCalculator: TemperatureCalculator[F]
  ): TemperatureDataAggregator[F] = new TemperatureDataAggregator[F] {
    
    override def aggregateDailyTemperatures(
      start: Instant,
      end: Instant
    ): F[Map[(String, String), Double]] = {
      for {
        readings <- readingsPort.getReadingsForPeriod(start, end)
        grouped = readings.groupBy(r => (r.macAddress, r.sensor))
        averages <- grouped.toList.traverse { case (key, readings) =>
          temperatureCalculator.calculateAverage(readings).map(avg => key -> avg)
        }
      } yield averages.toMap
    }
  }
}
