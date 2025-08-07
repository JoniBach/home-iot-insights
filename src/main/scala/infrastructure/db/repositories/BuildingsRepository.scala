package infrastructure.db.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import core.entities.Building
import core.ports.BuildingsPort
import java.util.UUID
import infrastructure.db.config.DatabaseConfig

final class DoobieBuildingsRepository extends BuildingsPort[IO] {
  
  // Import PostgreSQL UUID type support
  import doobie.postgres.implicits._
  
  override def findById(id: UUID): IO[Option[Building]] =
    sql"""SELECT id, created_at, name, address, postcode, latitude, longitude 
          FROM buildings WHERE id = $id"""
      .query[Building]
      .option
      .transact(DatabaseConfig.transactor)
  
  override def findAll: IO[List[Building]] =
    sql"""SELECT id, created_at, name, address, postcode, latitude, longitude 
          FROM buildings"""
      .query[Building]
      .to[List]
      .transact(DatabaseConfig.transactor)
  
  override def save(building: Building): IO[Building] = {
    val query = 
      sql"""INSERT INTO buildings (id, created_at, name, address, postcode, latitude, longitude)
            VALUES (${building.id}, ${building.createdAt}, ${building.name}, 
                   ${building.address}, ${building.postcode}, 
                   ${building.latitude}, ${building.longitude})
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                address = EXCLUDED.address,
                postcode = EXCLUDED.postcode,
                latitude = EXCLUDED.latitude,
                longitude = EXCLUDED.longitude
            RETURNING id, created_at, name, address, postcode, latitude, longitude"""
    
    query
      .query[Building]
      .unique
      .transact(DatabaseConfig.transactor)
  }
  
  override def delete(id: UUID): IO[Unit] =
    sql"DELETE FROM buildings WHERE id = $id"
      .update
      .run
      .transact(DatabaseConfig.transactor)
      .void
}
