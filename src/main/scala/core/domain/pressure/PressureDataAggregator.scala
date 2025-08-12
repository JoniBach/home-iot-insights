package core.domain.pressure

import core.entities.Reading
import core.ports.ReadingsPort
import cats.Monad
import cats.syntax.all._
import java.time.Instant

trait PressureDataAggregator[F[_]] {
    def aggregateDailyPressure(
        start: Instant,
        end: Instant
    ): F[Map[(String, String), Double]]
}

object PressureDataAggregator {
  def default[F[_]: Monad](
    readingsPort: ReadingsPort[F],
    pressureCalculator: PressureCalculator[F]
  ): PressureDataAggregator[F] = new PressureDataAggregator[F] {
// implament override

    override def aggregateDailyPressure(start: Instant, end: Instant): F[Map[(String, String), Double]] = {
        for {
            readings <- readingsPort.getReadingsForPeriod(start, end)
            grouped = readings.groupBy(r => (r.macAddress, r.sensor))
            averages <- grouped.toList.traverse { case (key, readings) => 
                pressureCalculator.calculateAverage(readings).map(avg => key -> avg)
                }
        } yield averages.toMap
    }
  }
}
