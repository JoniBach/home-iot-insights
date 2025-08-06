package core.entities

import java.sql.Timestamp
import java.util.UUID

final case class Building(
  id: UUID,
  createdAt: Timestamp,
  name: String,
  address: String = "",
  postcode: String = "",
  latitude: Option[Double] = None,
  longitude: Option[Double] = None
)
