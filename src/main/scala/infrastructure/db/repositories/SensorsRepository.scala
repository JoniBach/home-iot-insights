package infrastructure.db.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import core.entities.Sensor
import core.ports.SensorsPort
import java.util.UUID
import infrastructure.db.config.DatabaseConfig

final class DoSensorsRepository extends SensorsPort[IO] {
  
  // Import PostgreSQL UUID type support
  import doobie.postgres.implicits._
  
  override def findById(id: UUID): IO[Option[Sensor]] =
    sql"""SELECT id, key, created_at, name, description 
          FROM sensors WHERE id = $id"""
      .query[Sensor]
      .option
      .transact(DatabaseConfig.transactor)
      
  override def findByKey(key: String): IO[Option[Sensor]] =
    sql"""SELECT id, key, created_at, name, description 
          FROM sensors WHERE key = $key"""
      .query[Sensor]
      .option
      .transact(DatabaseConfig.transactor)
  
  override def findAll: IO[List[Sensor]] =
    sql"""SELECT id, key, created_at, name, description 
          FROM sensors"""
      .query[Sensor]
      .to[List]
      .transact(DatabaseConfig.transactor)
  
  override def save(sensor: Sensor): IO[Sensor] = {
    val query = 
      sql"""INSERT INTO sensors (id, key, created_at, name, description)
            VALUES (${sensor.id}, ${sensor.key}, ${sensor.createdAt}, 
                   ${sensor.name}, ${sensor.description})
            ON CONFLICT (id) DO UPDATE
            SET key = EXCLUDED.key,
                name = EXCLUDED.name,
                description = EXCLUDED.description
            RETURNING id, key, created_at, name, description"""
    
    query
      .query[Sensor]
      .unique
      .transact(DatabaseConfig.transactor)
  }
  
  override def delete(id: UUID): IO[Unit] =
    sql"DELETE FROM sensors WHERE id = $id"
      .update
      .run
      .transact(DatabaseConfig.transactor)
      .void
}
