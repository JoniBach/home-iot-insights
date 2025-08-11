package core.domain.context

import core.entities.DeviceRoomBuilding
import core.ports.{DeviceRoomBuildingsPort, SensorsPort}
import java.util.UUID

/**
 * Represents the context needed for temperature insights generation.
 * Contains relationships between devices, rooms, buildings, and sensor mappings.
 */
case class TemperatureContext(
  deviceRelationships: Map[String, DeviceRoomBuilding], // deviceId -> relationship
  sensorIds: Map[String, UUID] // sensorKey -> sensorId
)

/**
 * Provides context information for temperature calculations.
 */
trait TemperatureContextProvider[F[_]] {
  /**
   * Retrieves the context for the given devices and sensors.
   */
  def getContext(
    deviceIds: Set[String],
    sensorKeys: Set[String]
  ): F[TemperatureContext]
}

object TemperatureContextProvider {
  
  def default[F[_]: cats.Monad](
    deviceRoomBuildingsPort: DeviceRoomBuildingsPort[F],
    sensorsPort: SensorsPort[F]
  ): TemperatureContextProvider[F] = {
    val genericProvider = ContextProvider.create[F, TemperatureContext](
      deviceRoomBuildingsPort,
      sensorsPort,
      TemperatureContext.apply
    )
    
    new TemperatureContextProvider[F] {
      override def getContext(
        deviceIds: Set[String],
        sensorKeys: Set[String]
      ): F[TemperatureContext] = genericProvider.getContext(deviceIds, sensorKeys)
    }
  }
}
