package infrastructure.db.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import core.entities.Room
import core.ports.RoomRepository
import java.util.UUID
import infrastructure.db.config.DatabaseConfig

final class RoomsRepository extends RoomRepository[IO] {
  
  // Import PostgreSQL UUID type support
  import doobie.postgres.implicits._
  
  override def findById(id: UUID): IO[Option[Room]] =
    sql"""SELECT id, created_at, name, floor, building_id 
          FROM rooms WHERE id = $id"""
      .query[Room]
      .option
      .transact(DatabaseConfig.transactor)
  
  override def findByBuildingId(buildingId: UUID): IO[List[Room]] =
    sql"""SELECT id, created_at, name, floor, building_id 
          FROM rooms WHERE building_id = $buildingId"""
      .query[Room]
      .to[List]
      .transact(DatabaseConfig.transactor)
  
  override def save(room: Room): IO[Room] = {
    val query = 
      sql"""INSERT INTO rooms (id, created_at, name, floor, building_id)
            VALUES (${room.id}, ${room.createdAt}, ${room.name}, 
                   ${room.floor}, ${room.buildingId})
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                floor = EXCLUDED.floor,
                building_id = EXCLUDED.building_id
            RETURNING id, created_at, name, floor, building_id"""
    
    query
      .query[Room]
      .unique
      .transact(DatabaseConfig.transactor)
  }
  
  override def delete(id: UUID): IO[Unit] =
    sql"DELETE FROM rooms WHERE id = $id"
      .update
      .run
      .transact(DatabaseConfig.transactor)
      .void
}
