package core.ports

import cats.effect.IO
import core.entities.Sensor

import java.util.UUID

trait SensorRepository[F[_]] {
  def findById(id: UUID): F[Option[Sensor]]
  def findByKey(key: String): F[Option[Sensor]]
  def findAll: F[List[Sensor]]
  def save(sensor: Sensor): F[Sensor]
  def delete(id: UUID): F[Unit]
}
