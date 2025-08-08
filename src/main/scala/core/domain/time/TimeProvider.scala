package core.domain.time

import cats.Applicative
import cats.syntax.all._
import java.time.Instant
import java.time.{LocalDateTime, ZoneOffset}

/**
 * Provides time-related functionality for the application.
 * This allows for better testability by abstracting time operations.
 */
trait TimeProvider[F[_]] {
  /** Gets the current instant */
  def now: F[Instant]
  
  /** Gets the time range for the previous day (midnight to midnight) */
  def getPreviousDayRange: F[(Instant, Instant)]
  
  /** Gets the time range for N days ago (midnight to midnight) */
  def getDayRange(daysAgo: Int): F[(Instant, Instant)]
}

object TimeProvider {
  
  private def getMidnight(daysAgo: Int): Instant = LocalDateTime.now
    .minusDays(daysAgo)
    .withHour(0)
    .withMinute(0)
    .withSecond(0)
    .withNano(0)
    .toInstant(ZoneOffset.UTC)
  
  /** 
   * Default implementation using the system clock.
   * @tparam F The effect type that must have an Applicative instance
   */
  def default[F[_]: Applicative]: TimeProvider[F] = new TimeProvider[F] {
    override def now: F[Instant] = 
      Applicative[F].pure(Instant.now())
    
    override def getPreviousDayRange: F[(Instant, Instant)] = 
      getDayRange(1)
    
    override def getDayRange(daysAgo: Int): F[(Instant, Instant)] = {
      val start = getMidnight(daysAgo)
      val end = getMidnight(daysAgo - 1)
      Applicative[F].pure((start, end))
    }
  }
}
