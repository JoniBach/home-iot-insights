package core.domain.humidity

import core.entities.Reading
import cats.Monad
import cats.implicits._
import scala.math.BigDecimal.RoundingMode

/**
 * Service responsible for humidity-related calculations.
 */
trait HumidityCalculator[F[_]] {
  /**
   * Calculates the average humidity from a list of readings.
   * Returns 0.0 if the input list is empty.
   */
  def calculateAverage(readings: List[Reading]): F[Double]
  
  /**
   * Rounds a humidity value to 2 decimal places.
   */
  def roundHumidity(value: Double): F[Double]
}

object HumidityCalculator {
  /** Default implementation of HumidityCalculator */
  def default[F[_]: Monad]: HumidityCalculator[F] = new HumidityCalculator[F] {
    override def calculateAverage(readings: List[Reading]): F[Double] = {
      if (readings.nonEmpty) {
        val avg = readings.map(_.humidity).sum / readings.length
        roundHumidity(avg)
      } else {
        0.0.pure[F]
      }
    }
    
    override def roundHumidity(value: Double): F[Double] = {
      BigDecimal(value)
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble
        .pure[F]
    }
  }
}
