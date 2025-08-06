package core.entities

import java.time.Instant

final case class Alert(
  id: Int,
  timestamp: Instant,
  level: AlertLevel,
  message: String
)

sealed trait AlertLevel
object AlertLevel {
  case object Info extends AlertLevel
  case object Warning extends AlertLevel
  case object Critical extends AlertLevel
}
