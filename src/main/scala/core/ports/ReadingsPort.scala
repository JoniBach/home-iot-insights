package core.ports

import cats.effect.IO
import core.entities.Reading
import java.time.Instant

trait ReadingsPort[F[_]] {
  def getLatestReadings(limit: Int): F[List[Reading]]
  def getReadingsForPeriod(start: Instant, end: Instant): F[List[Reading]]
  def getAllReadings: F[List[Reading]]
  def getReadingsByDevice(macAddress: String): F[List[Reading]]
  def save(reading: Reading): F[Reading]
  def delete(id: Long): F[Unit]
}
