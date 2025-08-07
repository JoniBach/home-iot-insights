package infrastructure.db.repositories

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import core.entities.Insight
import core.ports.InsightsPort
import java.util.UUID
import java.time.Instant
import infrastructure.db.config.DatabaseConfig

final class DoobieInsightsRepository extends InsightsPort[IO] {
  import doobie.postgres.implicits._

  override def create(insight: Insight): IO[Insight] = {
    val query = 
      sql"""
        INSERT INTO insights (
          id, mac_address, sensor_id, value, 
          building_id, room_id, insight_type_id, 
          range_from, range_to, created_at
        )
        VALUES (
          ${insight.id.getOrElse(UUID.randomUUID())}, 
          ${insight.macAddress}, ${insight.sensor}, ${insight.value}, 
          ${insight.buildingId}, ${insight.roomId}, ${insight.insightTypeId},
          ${insight.rangeFrom}, ${insight.rangeTo}, ${insight.createdAt}
        )
        RETURNING 
          id, mac_address, sensor_id as sensor, value, 
          building_id, room_id, insight_type_id, 
          range_from, range_to, created_at
      """.query[Insight].unique

    query.transact(DatabaseConfig.transactor)
  }

  override def getById(id: UUID): IO[Option[Insight]] = {
    sql"""
      SELECT id, mac_address, sensor_id as sensor, value, 
             building_id, room_id, insight_type_id, 
             range_from, range_to, created_at
      FROM insights 
      WHERE id = $id
    """.query[Insight].option.transact(DatabaseConfig.transactor)
  }

  override def getBySensorId(sensorId: String): IO[List[Insight]] = {
    sql"""
      SELECT id, mac_address, sensor_id as sensor, value, 
             building_id, room_id, insight_type_id, 
             range_from, range_to, created_at
      FROM insights 
      WHERE sensor_id = $sensorId
      ORDER BY created_at DESC
    """.query[Insight].to[List].transact(DatabaseConfig.transactor)
  }

  override def getByBuildingId(buildingId: UUID): IO[List[Insight]] = {
    sql"""
      SELECT id, mac_address, sensor_id as sensor, value, 
             building_id, room_id, insight_type_id, 
             range_from, range_to, created_at
      FROM insights 
      WHERE building_id = $buildingId
      ORDER BY created_at DESC
    """.query[Insight].to[List].transact(DatabaseConfig.transactor)
  }

  override def getByDateRange(from: Instant, to: Instant): IO[List[Insight]] = {
    sql"""
      SELECT id, mac_address, sensor_id as sensor, value, 
             building_id, room_id, insight_type_id, 
             range_from, range_to, created_at
      FROM insights 
      WHERE created_at BETWEEN $from AND $to
      ORDER BY created_at DESC
    """.query[Insight].to[List].transact(DatabaseConfig.transactor)
  }

  override def update(insight: Insight): IO[Insight] = {
    val query = 
      sql"""
        UPDATE insights SET
          mac_address = ${insight.macAddress},
          sensor_id = ${insight.sensor},
          value = ${insight.value},
          building_id = ${insight.buildingId},
          room_id = ${insight.roomId},
          insight_type_id = ${insight.insightTypeId},
          range_from = ${insight.rangeFrom},
          range_to = ${insight.rangeTo}
        WHERE id = ${insight.id}
        RETURNING 
          id, mac_address, sensor_id as sensor, value, 
          building_id, room_id, insight_type_id, 
          range_from, range_to, created_at
      """.query[Insight].unique

    query.transact(DatabaseConfig.transactor)
  }

  override def delete(id: UUID): IO[Unit] = {
    sql"DELETE FROM insights WHERE id = $id"
      .update
      .run
      .transact(DatabaseConfig.transactor)
      .void
  }
}
