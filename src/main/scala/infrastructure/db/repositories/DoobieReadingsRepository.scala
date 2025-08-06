package infrastructure.db.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import core.entities.Reading
import core.ports.ReadingRepository
import java.time.Instant
import infrastructure.db.config.DatabaseConfig

final class DoobieReadingRepository extends ReadingRepository[IO] {
  
  def getLatestReadings(limit: Int): IO[List[Reading]] =
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure
          FROM readings
          ORDER BY created_at DESC
          LIMIT $limit"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)

  def getReadingsForPeriod(start: Instant, end: Instant): IO[List[Reading]] =
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure
          FROM readings
          WHERE created_at >= $start AND created_at <= $end
          ORDER BY created_at DESC"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)

  def getAllReadings: IO[List[Reading]] =
    getLatestReadings(10)

  def getReadingsByDevice(macAddress: String): IO[List[Reading]] =
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure
          FROM readings
          WHERE mac_address = $macAddress
          ORDER BY created_at DESC
          LIMIT 10"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)
}
