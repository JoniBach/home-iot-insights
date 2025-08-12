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
  val TemperatureAverage = InsightType(
    UUID.fromString("56da8d03-a64e-4395-8a7c-ebedbf8b122b"),
    "average_humidity_daily",
    Some("Average temperature over a day period")
  )
  
  // Humidity insight types
  val HumidityAverage = InsightType(
    UUID.fromString("c160c68c-0b82-4e1a-8bd8-6aab738c0266"),
    "average_temperature_daily",
    Some("Average humidity over a day period")
  )

  // pressure insight types
  val PressureAverage = InsightType(
    UUID.fromString("d1ab15e4-5b4e-460c-8e80-4a8fb7c5fe34"),
    "average_pressure_daily",
    Some("Average pressure over a day")
  )
  
  val AllTypes = List(
    TemperatureAverage,
    HumidityAverage,
    PressureAverage
  )
  
  def fromName(name: String): Option[InsightType] =
    AllTypes.find(_.name == name)
}
