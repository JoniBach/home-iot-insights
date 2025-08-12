import cats.effect.{IO, IOApp}
import cats.implicits._
import core.usecases.{GetLatestReadings}
import core.usecases.calculate.daily.average.CalculateDailyAverageTemperature
import core.usecases.calculate.daily.average.CalculateDailyAverageHumidity
import core.usecases.calculate.daily.average.CalculateDailyAveragePressure
import infrastructure.db.repositories._
import infrastructure.db.config.DatabaseConfig
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer

object Main extends IOApp.Simple {
  
  override def run: IO[Unit] = {
    // Initialize repositories
    val readingsPort = new DoobieReadingsRepository()
    val deviceRoomBuildingsPort = new DoobieDeviceRoomBuildingRepository()
    val sensorsPort = new DoSensorsRepository()
    val insightsPort = new DoobieInsightsRepository()

    // Initialize use cases
    val averageTemperature = CalculateDailyAverageTemperature.default[IO](
      readingsPort,
      deviceRoomBuildingsPort,
      sensorsPort,
      insightsPort
    )
    val averageHumidity = CalculateDailyAverageHumidity.default[IO](
      readingsPort,
      deviceRoomBuildingsPort,
      sensorsPort,
      insightsPort
    )
    val averagePressure = CalculateDailyAveragePressure.default[IO](
      readingsPort,
      deviceRoomBuildingsPort,
      sensorsPort,
      insightsPort
    )
    val getLatestReadings = new GetLatestReadings(readingsPort)

    // Configure pretty-printing for JSON output
    val jsonPrinter = Printer.spaces2.copy(dropNullValues = true)

    // Main program
    for {
      _ <- IO.println("=== Starting Home IoT Insights ===")
      _ <- IO.println("Fetching latest readings...")
      
      readings <- getLatestReadings.execute(5)
      _ <- IO.println(s"Latest readings: ${readings.mkString("\n")}")
      
      // Generate and display insights
      _ <- IO.println("\n=== Generating Insights ===")
      temperatureInsights <- averageTemperature.execute()
      humidityInsights <- averageHumidity.execute()
      pressureInsights <- averagePressure.execute()
      
      _ <- if (temperatureInsights.isEmpty) {
        IO.println("No temperature insights were generated. The database might be empty or there was an issue processing the data.")
      } else {
        IO.println(s"Generated ${temperatureInsights.length} temperature insights:") >>
        temperatureInsights.traverse_ { insight =>
          val json = jsonPrinter.print(insight.asJson)
          IO.println("-" * 80) >>
          IO.println(json)
        }
      }
      
      _ <- if (humidityInsights.isEmpty) {
        IO.println("No humidity insights were generated. The database might be empty or there was an issue processing the data.")
      } else {
        IO.println(s"Generated ${humidityInsights.length} humidity insights:") >>
        humidityInsights.traverse_ { insight =>
          val json = jsonPrinter.print(insight.asJson)
          IO.println("-" * 80) >>
          IO.println(json)
        }
      }

      _ <- if (pressureInsights.isEmpty) {
        IO.println("No pressure insights were generated. The database might be empty or there was an issue processing the data.")
      } else {
        IO.println(s"Generated ${pressureInsights.length} pressure insights:") >>
        pressureInsights.traverse_ { insight =>
          val json = jsonPrinter.print(insight.asJson)
          IO.println("-" * 80) >>
          IO.println(json)
        }
      }
        
      _ <- IO.println("=== Application Finished ===")
    } yield ()
  }.handleErrorWith { error =>
    IO.println(s"An error occurred: ${error.getMessage}")
  }
}