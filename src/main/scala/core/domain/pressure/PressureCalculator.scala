package core.domain.pressure

import core.entities.Reading
import cats.Monad
import cats.implicits._
import scala.math.BigDecimal.RoundingMode

trait PressureCalculator[F[_]] {
// calculate the averages from a list of readings
  def calculateAverage(readings: List[Reading]): F[Double]
// round a humidity to 2dp
  def roundHumidity(value: Double): F[Double]
}


object PressureCalculator {
    // this is our default
    def default[F[_]: Monad]: PressureCalculator[F] = new PressureCalculator[F] {
        override def calculateAverage(readings: List[Reading]): F[Double] = {
            // check to make sure there are readings to process
            if (readings.nonEmpty) {
                val avg = readings.map(_.humidity).sum / readings.length
                roundHumidity(avg)
            //  if not then lift 0.0 into the F context to meet type requirements
            } else {
                0.0.pure[F]
            }
        }

        override def roundHumidity(value: Double): F[Double] = {
            BigDecimal(value)
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble.pure[F]
        }
    }
    
}