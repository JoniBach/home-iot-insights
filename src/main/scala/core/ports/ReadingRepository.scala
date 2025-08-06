package core.ports

import core.entities.Reading
import java.time.Instant

trait ReadingRepository[F[_]] {
  def getLatestReadings(limit: Int): F[List[Reading]]
  def getReadingsForPeriod(start: Instant, end: Instant): F[List[Reading]]
}
