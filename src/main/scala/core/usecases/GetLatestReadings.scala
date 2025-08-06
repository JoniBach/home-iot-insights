package application.usecases

import cats.effect.IO
import core.entities.Reading
import core.ports.ReadingRepository

final class GetLatestReadings(repo: ReadingRepository[IO]) {
  def execute(limit: Int): IO[List[Reading]] =
    repo.getLatestReadings(limit)
}
