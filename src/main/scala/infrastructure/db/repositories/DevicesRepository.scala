package infrastructure.db.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import core.entities.Device
import core.ports.DeviceRepository
import java.util.UUID
import infrastructure.db.config.DatabaseConfig

final class DevicesRepository extends DeviceRepository[IO] {
  
  // Import PostgreSQL UUID type support
  import doobie.postgres.implicits._
  
  override def findById(id: UUID): IO[Option[Device]] =
    sql"""SELECT id, created_at, mac_address, name, connected_at 
          FROM devices WHERE id = $id"""
      .query[Device]
      .option
      .transact(DatabaseConfig.transactor)
      
  override def findByMacAddress(macAddress: String): IO[Option[Device]] =
    sql"""SELECT id, created_at, mac_address, name, connected_at 
          FROM devices WHERE mac_address = $macAddress"""
      .query[Device]
      .option
      .transact(DatabaseConfig.transactor)
  
  override def findAll: IO[List[Device]] =
    sql"""SELECT id, created_at, mac_address, name, connected_at 
          FROM devices"""
      .query[Device]
      .to[List]
      .transact(DatabaseConfig.transactor)
  
  override def save(device: Device): IO[Device] = {
    val query = 
      sql"""INSERT INTO devices (id, created_at, mac_address, name, connected_at)
            VALUES (${device.id}, ${device.createdAt}, ${device.macAddress}, 
                   ${device.name}, ${device.connectedAt})
            ON CONFLICT (id) DO UPDATE
            SET mac_address = EXCLUDED.mac_address,
                name = EXCLUDED.name,
                connected_at = EXCLUDED.connected_at
            RETURNING id, created_at, mac_address, name, connected_at"""
    
    query
      .query[Device]
      .unique
      .transact(DatabaseConfig.transactor)
  }
  
  override def delete(id: UUID): IO[Unit] =
    sql"DELETE FROM devices WHERE id = $id"
      .update
      .run
      .transact(DatabaseConfig.transactor)
      .void
}
