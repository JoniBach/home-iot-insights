package core.domain.time

import cats.Applicative
import cats.syntax.all._
import java.time.Instant
import java.time.{LocalDateTime, ZoneOffset}

/** Provides time-related functionality for the application. This allows for
  * better testability by abstracting time operations.
  */
trait TimeProvider[F[_]] {

  /** Gets the current instant */
  def now: F[Instant]

  /** Gets the time range for the previous day (midnight to midnight) */
  def getPreviousDayRange: F[(Instant, Instant)]

  /** Gets the time range for the previous week (midnight to midnight) */
  def getPreviousWeekRange: F[(Instant, Instant)]

  /** Gets the time range for the previous month (midnight to midnight) */
  def getPreviousMonthRange: F[(Instant, Instant)]

  /** Gets the time range for the previous year (midnight to midnight) */
  def getPreviousYearRange: F[(Instant, Instant)]

  /** Gets the time range for N days ago (midnight to midnight) */
  def getDayRange(daysAgo: Int): F[(Instant, Instant)]

  /** Gets the time range for N weeks ago (midnight to midnight) */
  def getWeekRange(weeksAgo: Int): F[(Instant, Instant)]

  /** Gets the time range for N months ago (midnight to midnight) */
  def getMonthRange(monthsAgo: Int): F[(Instant, Instant)]

  /** Gets the time range for N years ago (midnight to midnight) */
  def getYearRange(yearsAgo: Int): F[(Instant, Instant)]
}

object TimeProvider {

  private def getMidnight(daysAgo: Int): Instant = LocalDateTime.now
    .minusDays(daysAgo)
    .withHour(0)
    .withMinute(0)
    .withSecond(0)
    .withNano(0)
    .toInstant(ZoneOffset.UTC)

  /** Default implementation using the system clock.
    * @tparam F
    *   The effect type that must have an Applicative instance
    */
  def default[F[_]: Applicative]: TimeProvider[F] = new TimeProvider[F] {
    override def now: F[Instant] =
      Applicative[F].pure(Instant.now())

    override def getPreviousDayRange: F[(Instant, Instant)] =
      getDayRange(1)

    override def getPreviousWeekRange: F[(Instant, Instant)] =
      getWeekRange(1)

    override def getPreviousMonthRange: F[(Instant, Instant)] =
      getMonthRange(1)

    override def getPreviousYearRange: F[(Instant, Instant)] =
      getYearRange(1)

    override def getDayRange(daysAgo: Int): F[(Instant, Instant)] = {
      val start = getMidnight(daysAgo)
      val end = getMidnight(daysAgo - 1)
      Applicative[F].pure((start, end))
    }

    override def getWeekRange(weeksAgo: Int): F[(Instant, Instant)] = {
      val start = getMidnight(weeksAgo * 7)
      val end = getMidnight(weeksAgo * 7 - 7)
      Applicative[F].pure((start, end))
    }

    override def getMonthRange(monthsAgo: Int): F[(Instant, Instant)] = {
      val now = LocalDateTime.now(ZoneOffset.UTC)
      val endDate = now
        .minusMonths(monthsAgo - 1L)
        .`with`(java.time.temporal.TemporalAdjusters.firstDayOfMonth())
      val startDate = endDate.minusMonths(1)

      val start = startDate.toInstant(ZoneOffset.UTC)
      val end = endDate.toInstant(ZoneOffset.UTC)

      Applicative[F].pure((start, end))
    }

    override def getYearRange(yearsAgo: Int): F[(Instant, Instant)] = {
      val now = LocalDateTime.now(ZoneOffset.UTC)
      val endDate = now
        .minusYears(yearsAgo - 1L)
        .`with`(java.time.temporal.TemporalAdjusters.firstDayOfYear())
      val startDate = endDate.minusYears(1)

      val start = startDate.toInstant(ZoneOffset.UTC)
      val end = endDate.toInstant(ZoneOffset.UTC)

      Applicative[F].pure((start, end))
    }
  }
}
