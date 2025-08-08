package core.domain.context

import core.entities.DeviceRoomBuilding
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
    deviceRoomBuildingsPort: core.ports.DeviceRoomBuildingsPort[F],
    sensorsPort: core.ports.SensorsPort[F]
  ): TemperatureContextProvider[F] = new TemperatureContextProvider[F] {
    
    override def getContext(
      deviceIds: Set[String],
      sensorKeys: Set[String]
    ): F[TemperatureContext] = {
      import cats.syntax.all._
      
      for {
        // Fetch device relationships
        deviceRelationships <- deviceIds.toList.traverse { deviceId =>
          deviceRoomBuildingsPort.findByDeviceId(deviceId)
            .map(_.map(deviceId -> _))
        }
        
        // Fetch sensor IDs
        sensorIds <- sensorKeys.toList.traverse { sensorKey =>
          sensorsPort.findByKey(sensorKey)
            .map(_.map(sensor => sensorKey -> sensor.id))
        }
        
      } yield TemperatureContext(
        deviceRelationships = deviceRelationships.collect { case Some((id, rel)) => id -> rel }.toMap,
        sensorIds = sensorIds.collect { case Some((key, id)) => key -> id }.toMap
      )
    }
  }
}
