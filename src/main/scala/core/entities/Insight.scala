package core.entities

import java.time.Instant
import java.util.UUID

/**
 * Represents a processed insight derived from raw sensor readings.
 * 
 * @param id Unique identifier for the insight
 * @param macAddress MAC address of the device that generated the reading
 * @param sensor The sensor key that identifies the sensor
 * @param value The calculated value (e.g., average temperature)
 * @param buildingId Optional reference to the building where the device is located
 * @param roomId Optional reference to the room where the device is located
 * @param insightTypeId The type of insight (e.g., daily average temperature)
 * @param rangeFrom Start of the time period this insight covers
 * @param rangeTo End of the time period this insight covers
 * @param createdAt When this insight was generated
 * @param humidity Optional humidity reading if available
 * @param pressure Optional pressure reading if available
 */
final case class Insight(
  id: Option[UUID],
  macAddress: String,
  sensor: String,
  value: Double,
  buildingId: Option[UUID] = None,
  roomId: Option[UUID] = None,
  insightTypeId: Option[UUID] = None,
  rangeFrom: Option[Instant] = None,
  rangeTo: Option[Instant] = None,
  createdAt: Instant = Instant.now()
)
