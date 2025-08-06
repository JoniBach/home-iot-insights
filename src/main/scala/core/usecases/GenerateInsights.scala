package core.usecases

import core.entities.{Insight, Reading}
import core.ports.ReadingRepository

import cats.Monad
import cats.syntax.all._
import java.time.Instant

final class GenerateInsights[F[_]: Monad](
    readingRepo: ReadingRepository[F]
) {

  def execute(): F[List[Insight]] =
    readingRepo.getLatestReadings(limit = 10).map { readings =>
      // Just convert each Reading into a simple Insight with minimal data
      readings.map { r =>
        Insight(
          id = None,
          macAddress = r.macAddress,
          sensor = r.sensor,
          value = r.temperature,
          buildingId = None,
          roomId = None,
          insightTypeId = None,
          rangeFrom = None,
          rangeTo = None,
          createdAt = Instant.now()
        )
      }
    }
}
