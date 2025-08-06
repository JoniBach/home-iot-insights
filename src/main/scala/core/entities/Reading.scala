package core.entities

import java.sql.Timestamp

final case class Reading(
  id: Long,
  createdAt: Timestamp,
  macAddress: String,
  temperature: Double,
  humidity: Double,
  pressure: Double
)
