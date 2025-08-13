import cats.effect.{IO, IOApp}
import cats.implicits._
import core.usecases.{GetLatestReadings}
import core.usecases.calculate.daily.average.CalculateDailyAverageTemperature
import core.usecases.calculate.daily.average.CalculateDailyAverageHumidity
import core.usecases.calculate.daily.average.CalculateDailyAveragePressure
import core.usecases.calculate.weekly.average.CalculateWeeklyAverageHumidity
import core.usecases.calculate.weekly.average.CalculateWeeklyAveragePressure
import core.usecases.calculate.monthly.average.CalculateMonthlyAverageTemperature
import core.usecases.calculate.monthly.average.CalculateMonthlyAverageHumidity
import core.usecases.calculate.monthly.average.CalculateMonthlyAveragePressure
import core.usecases.calculate.yearly.average.CalculateYearlyAverageTemperature
import core.usecases.calculate.yearly.average.CalculateYearlyAverageHumidity
import core.usecases.calculate.yearly.average.CalculateYearlyAveragePressure
import infrastructure.db.repositories._
import infrastructure.db.config.DatabaseConfig
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer
import core.usecases.calculate.weekly.average.CalculateWeeklyAverageTemperature
import core.entities.Insight

def printInsights(
    prefix: String,
    jsonPrinter: Printer,
    insights: List[Insight]
): IO[Unit] = {
  insights.traverse_ { insight =>
    val json = jsonPrinter.print(insight.asJson)
    IO.println("-" * 80) >>
      IO.println(json) >>
      IO.println(prefix)
  }
}

def safePrintInsights(
    prefix: String,
    jsonPrinter: Printer,
    insights: List[Insight]
): IO[Unit] = {
  if (insights.isEmpty) {
    IO.println(s"No ${prefix} insights were generated.")
  } else {
    IO.println(s"${prefix} insights:") >> printInsights(prefix, jsonPrinter, insights)
  }
}

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
    val averageWeeklyTemperature =
      CalculateWeeklyAverageTemperature.default[IO](
        readingsPort,
        deviceRoomBuildingsPort,
        sensorsPort,
        insightsPort
      )
    val averageWeeklyHumidity = CalculateWeeklyAverageHumidity.default[IO](
      readingsPort,
      deviceRoomBuildingsPort,
      sensorsPort,
      insightsPort
    )
    val averageWeeklyPressure = CalculateWeeklyAveragePressure.default[IO](
      readingsPort,
      deviceRoomBuildingsPort,
      sensorsPort,
      insightsPort
    )

    val averageMonthlyTemperature =
      CalculateMonthlyAverageTemperature.default[IO](
        readingsPort,
        deviceRoomBuildingsPort,
        sensorsPort,
        insightsPort
      )
    val averageMonthlyHumidity = CalculateMonthlyAverageHumidity.default[IO](
      readingsPort,
      deviceRoomBuildingsPort,
      sensorsPort,
      insightsPort
    )
    val averageMonthlyPressure = CalculateMonthlyAveragePressure.default[IO](
      readingsPort,
      deviceRoomBuildingsPort,
      sensorsPort,
      insightsPort
    )
    val averageYearlyTemperature =
      CalculateYearlyAverageTemperature.default[IO](
        readingsPort,
        deviceRoomBuildingsPort,
        sensorsPort,
        insightsPort
      )
    val averageYearlyHumidity = CalculateYearlyAverageHumidity.default[IO](
      readingsPort,
      deviceRoomBuildingsPort,
      sensorsPort,
      insightsPort
    )
    val averageYearlyPressure = CalculateYearlyAveragePressure.default[IO](
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
      temperatureInsightsDaily <- averageTemperature.execute()
      humidityInsightsDaily <- averageHumidity.execute()
      pressureInsightsDaily <- averagePressure.execute()

      _ <- safePrintInsights("daily", jsonPrinter, temperatureInsightsDaily)
      _ <- safePrintInsights("daily", jsonPrinter, humidityInsightsDaily)
      _ <- safePrintInsights("daily", jsonPrinter, pressureInsightsDaily)
      temperatureInsightsWeekly <- averageWeeklyTemperature.execute()
      humidityInsightsWeekly <- averageWeeklyHumidity.execute()
      pressureInsightsWeekly <- averageWeeklyPressure.execute()

      _ <- safePrintInsights("weekly", jsonPrinter, temperatureInsightsWeekly)
      _ <- safePrintInsights("weekly", jsonPrinter, humidityInsightsWeekly)
      _ <- safePrintInsights("weekly", jsonPrinter, pressureInsightsWeekly)

      temperatureInsightsMonthly <- averageMonthlyTemperature.execute()
      humidityInsightsMonthly <- averageMonthlyHumidity.execute()
      pressureInsightsMonthly <- averageMonthlyPressure.execute()

      _ <- safePrintInsights("monthly", jsonPrinter, temperatureInsightsMonthly)
      _ <- safePrintInsights("monthly", jsonPrinter, humidityInsightsMonthly)
      _ <- safePrintInsights("monthly", jsonPrinter, pressureInsightsMonthly)

      temperatureInsightsYearly <- averageYearlyTemperature.execute()
      humidityInsightsYearly <- averageYearlyHumidity.execute()
      pressureInsightsYearly <- averageYearlyPressure.execute()

      _ <- safePrintInsights("yearly", jsonPrinter, temperatureInsightsYearly)
      _ <- safePrintInsights("yearly", jsonPrinter, humidityInsightsYearly)
      _ <- safePrintInsights("yearly", jsonPrinter, pressureInsightsYearly)

      _ <- IO.println("=== Application Finished ===")
    } yield ()
  }.handleErrorWith { error =>
    IO.println(s"An error occurred: ${error.getMessage}")
  }
}
