package core.ports

import cats.effect.IO
import core.entities.Building
import java.util.UUID

trait BuildingsPort[F[_]] {
  def findById(id: UUID): F[Option[Building]]
  def findAll: F[List[Building]]
  def save(building: Building): F[Building]
  def delete(id: UUID): F[Unit]
}