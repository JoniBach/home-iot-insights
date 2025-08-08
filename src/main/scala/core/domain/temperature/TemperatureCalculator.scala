package core.domain.temperature

import core.entities.Reading
import cats.Monad
import cats.implicits._
import scala.math.BigDecimal.RoundingMode

/**
 * Service responsible for temperature-related calculations.
 */
trait TemperatureCalculator[F[_]] {
  /**
   * Calculates the average temperature from a list of readings.
   * Returns 0.0 if the input list is empty.
   */
  def calculateAverage(readings: List[Reading]): F[Double]
  
  /**
   * Rounds a temperature value to 2 decimal places.
   */
  def roundTemperature(value: Double): F[Double]
}

object TemperatureCalculator {
  /** Default implementation of TemperatureCalculator */
  def default[F[_]: Monad]: TemperatureCalculator[F] = new TemperatureCalculator[F] {
    override def calculateAverage(readings: List[Reading]): F[Double] = {
      if (readings.nonEmpty) {
        val avg = readings.map(_.temperature).sum / readings.length
        roundTemperature(avg)
      } else {
        0.0.pure[F]
      }
    }
    
    override def roundTemperature(value: Double): F[Double] = {
      BigDecimal(value)
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble
        .pure[F]
    }
  }
}
