package core.ports

import core.entities.Insight

trait ReportOutputPort[F[_]] {
  def outputInsights(insights: List[Insight]): F[Unit]
}
