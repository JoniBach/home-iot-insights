package core.domain.context

import core.entities.DeviceRoomBuilding
import core.ports.{DeviceRoomBuildingsPort, SensorsPort}
import java.util.UUID
import cats.Monad
// define a case class that takes in a list of device relationships and a list of sensor IDs

// create a trait (or interface) that cprovides the infomration for  presure calculations such as retrieving the context for devices and sensors

// create an object for the context provider

case class PressureContext(
    deviceRelationships: Map[String, DeviceRoomBuilding],
    sensorIds: Map[String, UUID]
)

trait PressureContextProvider[F[_]] {

  def getContext(
      deviceIds: Set[String],
      sensorKeys: Set[String]
  ): F[PressureContext]

}

object PressureContextProvider {

  def default[F[_]: Monad](
      deviceRoomBuildingsPort: DeviceRoomBuildingsPort[F],
      sensorsPort: SensorsPort[F]
  ): PressureContextProvider[F] = {
    val genericProvider = ContextProvider.create[F, PressureContext](
      deviceRoomBuildingsPort,
      sensorsPort,
      PressureContext.apply
    )

    new PressureContextProvider[F] {
      override def getContext(
          deviceIds: Set[String],
          sensorKeys: Set[String]
      ): F[PressureContext] = genericProvider.getContext(deviceIds, sensorKeys)
    }
  }
}
