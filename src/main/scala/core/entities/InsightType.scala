package core.entities

import java.sql.Timestamp
import java.util.UUID

final case class InsightType(
  id: UUID,
  name: String,
  description: Option[String] = None,
  createdAt: Timestamp = new Timestamp(System.currentTimeMillis())
)

object InsightType {
  // Temperature insight types
  val TemperatureCurrent = InsightType(
    UUID.fromString("00000000-0000-0000-0000-000000000001"),
    "current_temperature",
    Some("Current temperature reading")
  )
  
  val TemperatureAverage = InsightType(
    UUID.fromString("00000000-0000-0000-0000-000000000002"),
    "average_temperature",
    Some("Average temperature over a time period")
  )
  
  // Humidity insight types
  val HumidityCurrent = InsightType(
    UUID.fromString("00000000-0000-0000-0000-000000000003"),
    "current_humidity",
    Some("Current humidity reading")
  )
  
  val HumidityAverage = InsightType(
    UUID.fromString("00000000-0000-0000-0000-000000000004"),
    "average_humidity",
    Some("Average humidity over a time period")
  )
  
  val AllTypes = List(
    TemperatureCurrent,
    TemperatureAverage,
    HumidityCurrent,
    HumidityAverage
  )
  
  def fromName(name: String): Option[InsightType] =
    AllTypes.find(_.name == name)
}
