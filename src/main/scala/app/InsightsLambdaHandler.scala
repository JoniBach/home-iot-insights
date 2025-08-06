package app

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import cats.effect.IO
import cats.effect.unsafe.implicits.global

import core.usecases.GenerateInsights
import infrastructure.db.repositories.DoobieReadingRepository

/**
 * AWS Lambda handler for generating insights from IoT sensor data.
 * 
 * This handler initializes the required dependencies and executes the
 * GenerateInsights use case when invoked.
 */
class InsightsLambdaHandler extends RequestHandler[Unit, String] {

  /**
   * Handles the Lambda function invocation.
   * 
   * @param input The input to the Lambda function (unused in this case)
   * @param context The Lambda execution environment context object
   * @return A string indicating the result of the operation
   */
  override def handleRequest(input: Unit, context: Context): String = {
    val logger = context.getLogger
    logger.log("Starting Insights generation job...")

    try {
      // Initialize dependencies
      val readingsRepo = new DoobieReadingRepository()
      val generateInsights = new GenerateInsights[IO](readingsRepo)

      // Execute the use case and handle the result
      val result = generateInsights.execute().attempt.unsafeRunSync()

      result match {
        case Right(insights) =>
          val message = s"Successfully generated ${insights.length} insights"
          logger.log(message)
          s"$message. First insight: ${insights.headOption.map(_.title).getOrElse("No insights")}"
          
        case Left(error) =>
          val errorMessage = s"Failed to generate insights: ${error.getMessage}"
          logger.log(errorMessage)
          errorMessage
      }
    } catch {
      case e: Exception =>
        val errorMessage = s"Unexpected error: ${e.getMessage}"
        logger.log(errorMessage)
        errorMessage
    }
  }
}