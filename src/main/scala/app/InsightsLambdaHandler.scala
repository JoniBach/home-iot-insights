package app

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import java.io.{PrintWriter, StringWriter}

import application.usecases.DailyAverageTemperature
import infrastructure.db.repositories.{DoobieDeviceRoomBuildingRepository, ReadingsRepository, SensorsRepository}
import cats.implicits._
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
      val readingsRepo = new ReadingsRepository()
      val deviceRoomBuildingRepo = new DoobieDeviceRoomBuildingRepository()
      val sensorRepo = new SensorsRepository()
      
      val averageTemperature = new DailyAverageTemperature(readingsRepo, deviceRoomBuildingRepo, sensorRepo)
      val jsonPrinter = Printer.spaces2.copy(dropNullValues = true)
      
      logger.log("=== Starting Daily Average Temperature Calculation ===")
      logger.log("Fetching and calculating average temperatures...\n")
      
      val result = averageTemperature.execute().attempt.unsafeRunSync()

      result match {
        case Right(insights) =>
          val message = s"=== Insights Generated ===\n" +
            s"Successfully generated ${insights.length} insights\n"
          
          val insightsLog = if (insights.isEmpty) {
            "No insights were generated. The database might be empty or there was an issue processing the data.\n"
          } else {
           insights.traverse_ { insight =>
        val json = jsonPrinter.print(insight.asJson)
        IO.println("-" * 80) *>
        IO.println(json)
      }
          }
          
          val fullMessage = message + insightsLog + "\n\n=== End of Insights ==="
          logger.log(fullMessage)
          fullMessage

        case Left(error) =>
          val errorMessage = s"❌ Failed to generate insights: ${error.getMessage}\n${stackTraceToString(error)}"
          logger.log(errorMessage)
          errorMessage
      }
    } catch {
      case e: Throwable =>
        val errorMessage = s"❌ Unexpected error: ${e.getMessage}\n${stackTraceToString(e)}"
        logger.log(errorMessage)
        errorMessage
    }
  }
}
