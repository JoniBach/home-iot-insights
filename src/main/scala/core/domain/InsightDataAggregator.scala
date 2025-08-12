package core.domain


import core.entities.Reading
import core.ports.ReadingsPort
import cats.Monad
import cats.syntax.all._
import java.time.Instant

import core.entities.Reading

/** Aggregates insight data for specific time periods.
  */
trait InsightDataAggregator[F[_]] {

  /** Aggregates daily insight data for the specified time range.
    * @return
    *   Map of (deviceId, sensorKey) to average insight
    */
  def aggregateDailyInsight(
      start: Instant,
      end: Instant
  ): F[Map[(String, String), Double]] // (deviceId, sensorKey) -> avgInsight

  def aggregateWeeklyInsight(
      start: Instant,
      end: Instant
  ): F[Map[(String, String), Double]] // (deviceId, sensorKey) -> avgInsight

  def aggregateMonthlyInsight(
      start: Instant,
      end: Instant
  ): F[Map[(String, String), Double]] // (deviceId, sensorKey) -> avgInsight

  def aggregateYearlyInsight(
      start: Instant,
      end: Instant
  ): F[Map[(String, String), Double]] // (deviceId, sensorKey) -> avgInsight
}

object InsightDataAggregator {

  def default[F[_]: Monad](
      readingsPort: ReadingsPort[F],
      insightCalculator: InsightCalculator[F],
      insightType: InsightType
  ): InsightDataAggregator[F] = new InsightDataAggregator[F] {

    override def aggregateDailyInsight(
        start: Instant,
        end: Instant
    ): F[Map[(String, String), Double]] = {
      for {
        readings <- readingsPort.getReadingsForPeriod(start, end)
        grouped = readings.groupBy(r => (r.macAddress, r.sensor))
        averages <- grouped.toList.traverse { case (key, readings) =>
          insightCalculator.calculateAverage(readings, insightType).map(avg => key -> avg)
        }
      } yield averages.toMap
    }

    override def aggregateWeeklyInsight(
        start: Instant,
        end: Instant
    ): F[Map[(String, String), Double]] = {
      for {
        readings <- readingsPort.getReadingsForPeriod(start, end)
        grouped = readings.groupBy(r => (r.macAddress, r.sensor))
        averages <- grouped.toList.traverse { case (key, readings) =>
          insightCalculator.calculateAverage(readings, insightType).map(avg => key -> avg)
        }
      } yield averages.toMap
    }

    override def aggregateMonthlyInsight(
        start: Instant,
        end: Instant
    ): F[Map[(String, String), Double]] = {
      for {
        readings <- readingsPort.getReadingsForPeriod(start, end)
        grouped = readings.groupBy(r => (r.macAddress, r.sensor))
        averages <- grouped.toList.traverse { case (key, readings) =>
          insightCalculator.calculateAverage(readings, insightType).map(avg => key -> avg)
        }
      } yield averages.toMap
    }

    override def aggregateYearlyInsight(
        start: Instant,
        end: Instant
    ): F[Map[(String, String), Double]] = {
      for {
        readings <- readingsPort.getReadingsForPeriod(start, end)
        grouped = readings.groupBy(r => (r.macAddress, r.sensor))
        averages <- grouped.toList.traverse { case (key, readings) =>
          insightCalculator.calculateAverage(readings, insightType).map(avg => key -> avg)
        }
      } yield averages.toMap
    }
  }
}
