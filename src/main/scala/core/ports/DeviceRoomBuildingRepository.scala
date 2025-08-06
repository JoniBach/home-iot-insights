package core.ports

import cats.effect.IO
import core.entities.DeviceRoomBuilding

import java.util.UUID

trait DeviceRoomBuildingRepository[F[_]] {
  /**
   * Find a device-room-building relationship by device ID (MAC address)
   */
  def findByDeviceId(deviceId: String): F[Option[DeviceRoomBuilding]]
  
  /**
   * Find all device-room-building relationships for a specific room
   */
  def findByRoomId(roomId: UUID): F[List[DeviceRoomBuilding]]
  
  /**
   * Find all device-room-building relationships for a specific building
   */
  def findByBuildingId(buildingId: UUID): F[List[DeviceRoomBuilding]]
  
  /**
   * Create or update a device-room-building relationship
   */
  def save(deviceRoomBuilding: DeviceRoomBuilding): F[DeviceRoomBuilding]
  
  /**
   * Delete a device-room-building relationship by ID
   */
  def delete(id: UUID): F[Unit]
}
