package core.entities

import java.time.Instant
import java.util.UUID

final case class Insight(
  id: Option[UUID],
  buildingId: Option[UUID],
  roomId: Option[UUID],
  sensorId: Option[UUID],
  insightTypeId: Option[UUID],
  deviceId: Option[String],
  createdAt: Instant,
  rangeFrom: Option[Instant],
  rangeTo: Option[Instant],
  value: Double,
)
