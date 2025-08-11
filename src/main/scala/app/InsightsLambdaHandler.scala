package app

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import java.io.{PrintWriter, StringWriter}
import infrastructure.db.repositories._
import infrastructure.db.config.DatabaseConfig
import core.usecases.calculate.daily.average.CalculateDailyAverageTemperature
import core.usecases.calculate.daily.average.CalculateDailyAverageHumidity
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer
class InsightsLambdaHandler extends RequestHandler[Unit, String] {

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
      
      // Configure JSON pretty printing
      val jsonPrinter = Printer.spaces2.copy(dropNullValues = true)
      
      logger.log("=== Starting Daily Average Temperature Calculation ===")
      logger.log("Fetching and calculating average temperatures...\n")
      val temperatureInsights = averageTemperature.execute().unsafeRunSync()
      
      logger.log("=== Starting Daily Average Humidity Calculation ===")
      logger.log("Fetching and calculating average humidity...\n")
      val humidityInsights = averageHumidity.execute().unsafeRunSync()
      
      
      val message = new StringBuilder()
      
      message.append("=== Insights Generated ===\n")
      message.append(s"Successfully generated ${temperatureInsights.length} temperature insights\n\n")
      message.append(s"Successfully generated ${humidityInsights.length} humidity insights\n\n")
      
      if (temperatureInsights.isEmpty) {
        message.append("No temperature insights were generated. The database might be empty or there was an issue processing the data.\n")
      } else {
        temperatureInsights.foreach { insight =>
          val json = jsonPrinter.print(insight.asJson)
          message.append("-" * 80).append("\n")
          message.append(json).append("\n")
        }
      }

      if (humidityInsights.isEmpty) {
        message.append("No humidity insights were generated. The database might be empty or there was an issue processing the data.\n")
      } else {
        humidityInsights.foreach { insight =>
          val json = jsonPrinter.print(insight.asJson)
          message.append("-" * 80).append("\n")
          message.append(json).append("\n")
        }
      }
      
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
