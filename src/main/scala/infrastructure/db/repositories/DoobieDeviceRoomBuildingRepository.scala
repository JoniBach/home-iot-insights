package infrastructure.db.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import core.entities.DeviceRoomBuilding
import core.ports.DeviceRoomBuildingsPort
import infrastructure.db.config.DatabaseConfig

import java.util.UUID

final class DoobieDeviceRoomBuildingRepository extends DeviceRoomBuildingsPort[IO] {
  
  // Import PostgreSQL UUID type support
  import doobie.postgres.implicits._
  
  override def findByDeviceId(deviceId: String): IO[Option[DeviceRoomBuilding]] =
    sql"""SELECT id, device_id, room_id, building_id 
          FROM device_room_building 
          WHERE device_id = $deviceId"""
      .query[DeviceRoomBuilding]
      .option
      .transact(DatabaseConfig.transactor)
  
  override def findByRoomId(roomId: UUID): IO[List[DeviceRoomBuilding]] =
    sql"""SELECT id, device_id, room_id, building_id 
          FROM device_room_building 
          WHERE room_id = $roomId"""
      .query[DeviceRoomBuilding]
      .to[List]
      .transact(DatabaseConfig.transactor)
  
  override def findByBuildingId(buildingId: UUID): IO[List[DeviceRoomBuilding]] =
    sql"""SELECT id, device_id, room_id, building_id 
          FROM device_room_building 
          WHERE building_id = $buildingId"""
      .query[DeviceRoomBuilding]
      .to[List]
      .transact(DatabaseConfig.transactor)
  
  override def save(deviceRoomBuilding: DeviceRoomBuilding): IO[DeviceRoomBuilding] = {
    val query = 
      sql"""INSERT INTO device_room_building (id, device_id, room_id, building_id)
            VALUES (${deviceRoomBuilding.id}, ${deviceRoomBuilding.deviceId}, 
                   ${deviceRoomBuilding.roomId}, ${deviceRoomBuilding.buildingId})
            ON CONFLICT (id) DO UPDATE
            SET room_id = EXCLUDED.room_id,
                building_id = EXCLUDED.building_id
            RETURNING id, device_id, room_id, building_id"""
    
    query
      .query[DeviceRoomBuilding]
      .unique
      .transact(DatabaseConfig.transactor)
  }
  
  override def delete(id: UUID): IO[Unit] =
    sql"DELETE FROM device_room_building WHERE id = $id"
      .update
      .run
      .transact(DatabaseConfig.transactor)
      .void
}
