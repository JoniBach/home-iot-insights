package core.domain.context
import cats.syntax.applicative._  // This is the most common and recommended way

import core.entities.DeviceRoomBuilding
import java.util.UUID

/**
 * Represents the context needed for humidity insights generation.
 * Contains relationships between devices, rooms, buildings, and sensor mappings.
 */
case class HumidityContext(
  deviceRelationships: Map[String, DeviceRoomBuilding], // deviceId -> relationship
  sensorIds: Map[String, UUID] // sensorKey -> sensorId
)

/**
 * Provides context information for humidity calculations.
 */
trait HumidityContextProvider[F[_]] {
  /**
   * Retrieves the context for the given devices and sensors.
   */
  def getContext(
    deviceIds: Set[String],
    sensorKeys: Set[String]
  ): F[HumidityContext]
}

object HumidityContextProvider {
  
  def default[F[_]: cats.Monad](
    deviceRelationships: Map[String, DeviceRoomBuilding],
    sensorIds: Map[String, UUID]
  ): HumidityContextProvider[F] = new HumidityContextProvider[F] {
    override def getContext(
      deviceIds: Set[String],
      sensorKeys: Set[String]
    ): F[HumidityContext] = 
      HumidityContext(
        deviceRelationships = deviceRelationships.view.filterKeys(deviceIds.contains).toMap,
        sensorIds = sensorIds.view.filterKeys(sensorKeys.contains).toMap
      ).pure[F]
  }
  
  // Add any additional factory methods or utilities here
}
