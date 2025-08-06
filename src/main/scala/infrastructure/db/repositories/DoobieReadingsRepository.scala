package infrastructure.db.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import core.entities.Reading
import core.ports.ReadingRepository
import java.time.Instant
import java.util.UUID
import infrastructure.db.config.DatabaseConfig

final class ReadingsRepository extends ReadingRepository[IO] {
  
  // Import PostgreSQL UUID type support
  import doobie.postgres.implicits._
  
  override def getLatestReadings(limit: Int): IO[List[Reading]] =
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure, sensor
          FROM readings
          ORDER BY created_at DESC
          LIMIT $limit"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)

  override def getReadingsForPeriod(start: Instant, end: Instant): IO[List[Reading]] =
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure, sensor
          FROM readings
          WHERE created_at >= $start AND created_at <= $end
          ORDER BY created_at DESC"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)

  override def getAllReadings: IO[List[Reading]] =
    getLatestReadings(10)

  override def getReadingsByDevice(macAddress: String): IO[List[Reading]] =
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure, sensor
          FROM readings
          WHERE mac_address = $macAddress
          ORDER BY created_at DESC
          LIMIT 10"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)
      
  override def save(reading: Reading): IO[Reading] =
    sql"""INSERT INTO readings (id, created_at, mac_address, temperature, humidity, pressure, sensor)
          VALUES (${reading.id}, ${reading.createdAt}, ${reading.macAddress}, 
                 ${reading.temperature}, ${reading.humidity}, ${reading.pressure}, ${reading.sensor})
          ON CONFLICT (id) DO UPDATE
          SET temperature = EXCLUDED.temperature,
              humidity = EXCLUDED.humidity,
              pressure = EXCLUDED.pressure,
              sensor = EXCLUDED.sensor
          RETURNING id, created_at, mac_address, temperature, humidity, pressure, sensor"""
      .query[Reading]
      .unique
      .transact(DatabaseConfig.transactor)
      
  override def delete(id: Long): IO[Unit] =
    sql"DELETE FROM readings WHERE id = $id"
      .update
      .run
      .transact(DatabaseConfig.transactor)
      .void
}
