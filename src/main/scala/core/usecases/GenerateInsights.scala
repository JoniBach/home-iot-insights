package core.usecases

import core.entities.{Insight, Reading}
import core.ports.ReadingRepository

import cats.Monad
import cats.syntax.all._

final class GenerateInsights[F[_]: Monad](
    readingRepo: ReadingRepository[F]
) {

  def execute(): F[List[Insight]] =
    readingRepo.getLatestReadings(limit = 10).map { readings =>
      // Just convert each Reading into a simple Insight with minimal data
      readings.map { r =>
        Insight(
          id = r.id.toInt,
          title = s"Reading from ${r.macAddress}",
          description = s"Temperature: ${r.temperature}°C, Humidity: ${r.humidity}%",
          value = s"At ${r.createdAt}"
        )
      }
    }
}
