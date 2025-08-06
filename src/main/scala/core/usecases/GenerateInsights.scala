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
          buildingId = None,
          roomId = None,
          sensorId = None,
          insightTypeId = None,
          deviceId = None,
          createdAt = Instant.now(),
          rangeFrom = None,
          rangeTo = None,
          value = r.temperature
        )
      }
    }
}
