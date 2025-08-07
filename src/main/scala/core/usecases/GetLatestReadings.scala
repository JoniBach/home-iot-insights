package core.usecases

import cats.effect.IO
import core.entities.Reading
import core.ports.ReadingsPort

final class GetLatestReadings(readingsPort: ReadingsPort[IO]) {
  def execute(limit: Int): IO[List[Reading]] =
    readingsPort.getLatestReadings(limit)
}
