package app

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import java.io.{PrintWriter, StringWriter}

import core.usecases.GenerateInsights
import infrastructure.db.repositories.ReadingsRepository

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
      val generateInsights = new GenerateInsights[IO](readingsRepo)

      val result = generateInsights.execute().attempt.unsafeRunSync()

      result match {
        case Right(insights) =>
          val message = s"Successfully generated ${insights.length} insights"
          logger.log(message)
          s"$message. First insight: ${insights.headOption.map(_.id).getOrElse("No insights")}"

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
