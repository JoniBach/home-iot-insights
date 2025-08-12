package core.domain

import core.entities.Reading
import cats.Monad
import cats.implicits._
import scala.math.BigDecimal.RoundingMode

/**
 * Service responsible for insight-related calculations.
 */
trait InsightCalculator[F[_]] {
  /**
   * Calculates the average insight from a list of readings.
   * Returns 0.0 if the input list is empty.
   */
  def calculateAverage(readings: List[Reading], insightType: InsightType): F[Double]
  
  /**
   * Rounds a insight value to 2 decimal places.
   */
  def roundInsight(value: Double): F[Double]
}

object InsightCalculator {
  /** Default implementation of InsightCalculator */
  def default[F[_]: Monad]: InsightCalculator[F] = new InsightCalculator[F] {
    override def calculateAverage(readings: List[Reading], insightType: InsightType): F[Double] = {
      if (readings.nonEmpty) {
        val values = readings.map(insightType.extractValue)
        val avg = values.sum / values.length
        roundInsight(avg)
      } else {
        0.0.pure[F]
      }
    }
    
    override def roundInsight(value: Double): F[Double] = {
      BigDecimal(value)
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble
        .pure[F]
    }
  }
}
