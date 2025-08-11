package core.domain.context

import core.entities.DeviceRoomBuilding
import core.ports.{DeviceRoomBuildingsPort, SensorsPort}
import java.util.UUID
import cats.Monad

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
  
  def default[F[_]: Monad](
    deviceRoomBuildingsPort: DeviceRoomBuildingsPort[F],
    sensorsPort: SensorsPort[F]
  ): HumidityContextProvider[F] = {
    val genericProvider = ContextProvider.create[F, HumidityContext](
      deviceRoomBuildingsPort,
      sensorsPort,
      HumidityContext.apply
    )
    
    new HumidityContextProvider[F] {
      override def getContext(
        deviceIds: Set[String],
        sensorKeys: Set[String]
      ): F[HumidityContext] = genericProvider.getContext(deviceIds, sensorKeys)
    }
  }
}
