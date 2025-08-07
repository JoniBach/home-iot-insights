package core.ports

import cats.effect.IO
import core.entities.Room
import java.util.UUID

trait RoomsPort[F[_]] {
  def findById(id: UUID): F[Option[Room]]
  def findByBuildingId(buildingId: UUID): F[List[Room]]
  def save(room: Room): F[Room]
  def delete(id: UUID): F[Unit]
}