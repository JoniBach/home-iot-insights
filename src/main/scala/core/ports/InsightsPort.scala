package core.ports

import cats.effect.IO
import core.entities.Insight
import java.util.UUID
import java.time.Instant

trait InsightsPort[F[_]] {
  def create(insight: Insight): F[Insight]
  def getById(id: UUID): F[Option[Insight]]
  def getBySensorId(sensorId: String): F[List[Insight]]
  def getByBuildingId(buildingId: UUID): F[List[Insight]]
  def getByDateRange(from: Instant, to: Instant): F[List[Insight]]
  def update(insight: Insight): F[Insight]
  def delete(id: UUID): F[Unit]
}
