package core.domain.humidity

import core.entities.Reading
import core.ports.ReadingsPort
import cats.Monad
import cats.syntax.all._
import java.time.Instant

/** Aggregates humidity data for specific time periods.
  */
trait HumidityDataAggregator[F[_]] {

  /** Aggregates daily humidity data for the specified time range.
    * @return
    *   Map of (deviceId, sensorKey) to average humidity
    */
  def aggregateDailyHumidity(
      start: Instant,
      end: Instant
  ): F[Map[(String, String), Double]] // (deviceId, sensorKey) -> avgHumidity
}

object HumidityDataAggregator {

  def default[F[_]: Monad](
      readingsPort: ReadingsPort[F],
      humidityCalculator: HumidityCalculator[F]
  ): HumidityDataAggregator[F] = new HumidityDataAggregator[F] {

    override def aggregateDailyHumidity(
        start: Instant,
        end: Instant
    ): F[Map[(String, String), Double]] = {
      for {
        readings <- readingsPort.getReadingsForPeriod(start, end)
        grouped = readings.groupBy(r => (r.macAddress, r.sensor))
        averages <- grouped.toList.traverse { case (key, readings) =>
          humidityCalculator.calculateAverage(readings).map(avg => key -> avg)
        }
      } yield averages.toMap
    }
  }
}
