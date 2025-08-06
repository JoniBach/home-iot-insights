package core.ports

import core.entities.Alert

trait AlertNotificationPort[F[_]] {
  def sendAlert(alert: Alert): F[Unit]
}
