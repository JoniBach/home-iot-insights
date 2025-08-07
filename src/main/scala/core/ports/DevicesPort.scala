package core.ports

import cats.effect.IO
import core.entities.Device

import java.util.UUID

trait DevicesPort[F[_]] {
  def findById(id: UUID): F[Option[Device]]
  def findByMacAddress(macAddress: String): F[Option[Device]]
  def findAll: F[List[Device]]
  def save(device: Device): F[Device]
  def delete(id: UUID): F[Unit]
}
