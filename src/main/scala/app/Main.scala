import cats.effect.{IO, IOApp}
import cats.implicits._
import application.usecases.{GetLatestReadings, DailyAverageTemperature}
import infrastructure.db.repositories.ReadingsRepository
import infrastructure.db.repositories.DoobieDeviceRoomBuildingRepository
import infrastructure.db.repositories.SensorsRepository
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    val readingRepo = new ReadingsRepository()
    val deviceRoomBuildingRepo = new DoobieDeviceRoomBuildingRepository()
    val sensorRepo = new SensorsRepository()

    val averageTemperature = new DailyAverageTemperature(readingRepo, deviceRoomBuildingRepo, sensorRepo)

    // Configure pretty-printing for JSON output
    val jsonPrinter = Printer.spaces2.copy(dropNullValues = true)

    val program = for {
      _ <- IO.println("=== Starting Daily Average Temperature Calculation ===")
      _ <- IO.println("Fetching and calculating average temperatures...\n")
      
      insights <- averageTemperature.execute()
      
      _ <- IO.println("=== Insights Generated ===")
      _ <- if (insights.isEmpty) IO.println("No insights were generated. The database might be empty or there was an issue processing the data.")
          else IO.unit
          
      _ <- insights.traverse_ { insight =>
        val json = jsonPrinter.print(insight.asJson)
        IO.println("-" * 80) *>
        IO.println(json)
      }
      
      _ <- IO.println(s"\n=== End of Insights (${insights.length} insights) ===")
    } yield ()

    program.handleErrorWith { error =>
      IO.println(s"Database connection failed: ${error.getMessage}")
    }
  }
}