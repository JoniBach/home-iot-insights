package core.entities

import java.sql.Timestamp
import java.util.UUID

final case class Sensor(
  id: UUID,
  key: String,
  createdAt: Timestamp,
  name: String,
  description: String = ""
)
