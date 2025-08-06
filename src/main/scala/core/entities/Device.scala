package core.entities

import java.sql.Timestamp
import java.util.UUID

final case class Device(
  id: UUID,
  createdAt: Timestamp,
  macAddress: String,
  name: String = "",
  connectedAt: Option[Long] = None
)
