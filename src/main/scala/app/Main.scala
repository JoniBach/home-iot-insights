import cats.effect.{IO, IOApp}
import cats.implicits._
import application.usecases.{GetLatestReadings, DailyAverageTemperature}
import infrastructure.db.repositories.ReadingsRepository
import infrastructure.db.repositories.DoobieDeviceRoomBuildingRepository
import infrastructure.db.repositories.SensorsRepository

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    val readingRepo = new ReadingsRepository()
    val deviceRoomBuildingRepo = new DoobieDeviceRoomBuildingRepository()
    val sensorRepo = new SensorsRepository()

    val averageTemperature = new DailyAverageTemperature(readingRepo, deviceRoomBuildingRepo, sensorRepo)

    val program = for {
      _ <- IO.println("=== Starting Daily Average Temperature Calculation ===")
      _ <- IO.println("Fetching and calculating average temperatures...\n")
      
      insights <- averageTemperature.execute()
      
      _ <- IO.println("=== Insights Generated ===")
      _ <- if (insights.isEmpty) IO.println("No insights were generated. The database might be empty or there was an issue processing the data.")
          else IO.unit
          
      _ <- insights.traverse_ { insight =>
        IO.println("-" * 80) *>
        IO.println(s"Device ID: ${insight.deviceId.getOrElse("N/A")}") *>
        IO.println(s"Average Temperature: ${insight.value}°C") *>
        IO.println(s"Building ID: ${insight.buildingId.getOrElse("N/A")}") *>
        IO.println(s"Room ID: ${insight.roomId.getOrElse("N/A")}") *>
        IO.println(s"Sensor ID: ${insight.sensorId.getOrElse("N/A")}") *>
        IO.println(s"Time Range: ${insight.rangeFrom.getOrElse("N/A")} to ${insight.rangeTo.getOrElse("N/A")}") *>
        IO.println(s"Created At: ${insight.createdAt}")
      }
      
      _ <- IO.println("\n=== End of Insights ===")
    } yield ()

    program.handleErrorWith { error =>
      IO.println(s"Database connection failed: ${error.getMessage}")
    }
  }
}