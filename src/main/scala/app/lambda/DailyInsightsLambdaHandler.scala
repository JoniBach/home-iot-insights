package app

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import java.io.{PrintWriter, StringWriter}
import infrastructure.db.repositories._
import infrastructure.db.config.DatabaseConfig
import core.usecases.calculate.daily.average.{CalculateDailyAverageTemperature, CalculateDailyAverageHumidity, CalculateDailyAveragePressure}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer
import core.entities.Insight

class InsightsDailyLambdaHandler extends RequestHandler[Unit, String] {

  private def stackTraceToString(t: Throwable): String = {
    val sw = new StringWriter()
    val pw = new PrintWriter(sw)
    t.printStackTrace(pw)
    sw.toString
  }

  override def handleRequest(input: Unit, context: Context): String = {
    val logger = context.getLogger
    
    try {
      // Initialize repositories
      val readingsPort = new DoobieReadingsRepository()
      val deviceRoomBuildingsPort = new DoobieDeviceRoomBuildingRepository()
      val sensorsPort = new DoSensorsRepository()
      val insightsPort = new DoobieInsightsRepository()
      val message = new StringBuilder()
      
      // Initialize use case with default dependencies
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
      
      // Configure JSON pretty printing
      val jsonPrinter = Printer.spaces2.copy(dropNullValues = true)
      
      logger.log("=== Starting Daily Average Temperature Calculation ===")
      val temperatureInsights = averageTemperature.execute().unsafeRunSync()
      
      logger.log("=== Starting Daily Average Humidity Calculation ===")
      val humidityInsights = averageHumidity.execute().unsafeRunSync()
      
      logger.log("=== Starting Daily Average Pressure Calculation ===")
      val pressureInsights = averagePressure.execute().unsafeRunSync()
      
      
      message.append("=== Insights Generated ===\n")
      message.append(s"${temperatureInsights.length} temperature insights\n\n")
      message.append(s"${humidityInsights.length} humidity insights\n\n")
      message.append(s"${pressureInsights.length} pressure insights\n\n")
      
      // logging example for troubleshooting
      // if (temperatureInsights.isEmpty) {
      //   message.append("No temperature insights were generated. The database might be empty or there was an issue processing the data.\n")
      // } else {
      //   temperatureInsights.foreach { insight =>
      //     val json = jsonPrinter.print(insight.asJson)
      //     message.append("-" * 80).append("\n")
      //     message.append(json).append("\n")
      //   }
      // }

      message.append("\n=== End of Insights ===\n")
      val fullMessage = message.toString()
      logger.log(fullMessage)
      fullMessage
    } catch {
      case e: Throwable =>
        val errorMessage = s"❌ Unexpected error: ${e.getMessage}\n${stackTraceToString(e)}"
        logger.log(errorMessage)
        errorMessage
    }
  }
}
