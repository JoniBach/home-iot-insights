package app

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.implicits._
import java.io.{PrintWriter, StringWriter}
import infrastructure.db.repositories._
import infrastructure.db.config.DatabaseConfig
import core.usecases.calculate.weekly.average.{CalculateWeeklyAverageTemperature, CalculateWeeklyAverageHumidity, CalculateWeeklyAveragePressure}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer
import core.entities.Insight

class WeeklyInsightsLambdaHandler extends RequestHandler[Unit, String] {

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
      val averageTemperature = CalculateWeeklyAverageTemperature.default[IO](
        readingsPort,
        deviceRoomBuildingsPort,
        sensorsPort,
        insightsPort
      )

      val averageHumidity = CalculateWeeklyAverageHumidity.default[IO](
        readingsPort,
        deviceRoomBuildingsPort,
        sensorsPort,
        insightsPort
      )

      val averagePressure = CalculateWeeklyAveragePressure.default[IO](
        readingsPort,
        deviceRoomBuildingsPort,
        sensorsPort,
        insightsPort
      )
      
      // Configure JSON pretty printing
      val jsonPrinter = Printer.spaces2.copy(dropNullValues = true)
      
      logger.log("=== Starting Weekly Average Temperature Calculation ===")
      val temperatureInsights = averageTemperature.execute().unsafeRunSync()
      
      logger.log("=== Starting Weekly Average Humidity Calculation ===")
      val humidityInsights = averageHumidity.execute().unsafeRunSync()
      
      logger.log("=== Starting Weekly Average Pressure Calculation ===")
      val pressureInsights = averagePressure.execute().unsafeRunSync()
      
      
      message.append("=== Insights Generated ===\n")
      message.append(s"${temperatureInsights.length} temperature insights\n\n")
      message.append(s"${humidityInsights.length} humidity insights\n\n")
      message.append(s"${pressureInsights.length} pressure insights\n\n")
      
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
