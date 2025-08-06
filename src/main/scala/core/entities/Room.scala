package core.entities

import java.sql.Timestamp
import java.util.UUID

final case class Room(
  id: UUID,
  createdAt: Timestamp,
  name: String = "",
  floor: Option[Int] = None,
  buildingId: UUID
)
