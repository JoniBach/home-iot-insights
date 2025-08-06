package core.entities

import java.sql.Timestamp
import java.util.UUID

final case class InsightType(
  id: UUID,
  name: String,
  description: Option[String] = None,
  createdAt: Timestamp = new Timestamp(System.currentTimeMillis())
)
