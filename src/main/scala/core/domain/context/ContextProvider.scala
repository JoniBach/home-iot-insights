package core.domain.context

import core.entities.DeviceRoomBuilding
import core.ports.{DeviceRoomBuildingsPort, SensorsPort}
import cats.Monad
import cats.syntax.all._
import java.util.UUID

/**
 * A generic context provider that can be used for different types of sensor data.
 * @tparam F the effect type
 * @tparam C the context type
 */
private[context] trait GenericContextProvider[F[_], C] {
  def getContext(
    deviceIds: Set[String],
    sensorKeys: Set[String]
  ): F[C]
}

/**
 * Helper object for creating context providers with common functionality.
 */
private[context] object ContextProvider {
  
  /**
   * Creates a context provider with the given context constructor and data access methods.
   */
  def create[F[_]: Monad, C](
    deviceRoomBuildingsPort: DeviceRoomBuildingsPort[F],
    sensorsPort: SensorsPort[F],
    contextConstructor: (Map[String, DeviceRoomBuilding], Map[String, UUID]) => C
  ): GenericContextProvider[F, C] = new GenericContextProvider[F, C] {
    
    override def getContext(
      deviceIds: Set[String],
      sensorKeys: Set[String]
    ): F[C] = {
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
        
      } yield contextConstructor(
        deviceRelationships.collect { case Some((id, rel)) => id -> rel }.toMap,
        sensorIds.collect { case Some((key, id)) => key -> id }.toMap
      )
    }
  }
}
