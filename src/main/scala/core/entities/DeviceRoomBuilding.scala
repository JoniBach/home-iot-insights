package core.entities

import java.sql.Timestamp
import java.util.UUID

/**
 * Represents the relationship between a device, room, and building.
 * 
 * @param id The unique identifier for this relationship
 * @param deviceId The MAC address of the device
 * @param roomId The ID of the room where the device is located (optional)
 * @param buildingId The ID of the building where the device is located (optional)
 */
final case class DeviceRoomBuilding(
  id: UUID,
  deviceId: String,
  roomId: Option[UUID] = None,
  buildingId: Option[UUID] = None
)
