package app

import cats.effect.{ExitCode, IO, IOApp}
import core.entities.Insight
import infrastructure.db.repositories.DoobieInsightsRepository
import java.util.UUID
import java.time.Instant

object InsightsRepositoryExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val repository = new DoobieInsightsRepository()
    
    // Create a new insight
    val newInsight = Insight(
      id = None,  // Will be auto-generated
      macAddress = "b'T2\\x04\\x1do\\xa4'",
      sensor = "m5_env_4",
      value = 22.5,
      buildingId = Some(UUID.fromString("c1291ca4-e859-44b9-b738-984babb9fd2d")),
      roomId = Some(UUID.fromString("ccdd7e22-c327-4fe7-8d8b-a7d66aaeea1a")),
      insightTypeId = Some(UUID.fromString("c160c68c-0b82-4e1a-8bd8-6aab738c0266")),
      rangeFrom = Some(Instant.parse("2025-08-06T00:00:00Z")),
      rangeTo = Some(Instant.parse("2025-08-07T00:00:00Z")),
      createdAt = Instant.now()
    )

    val program = for {
      // Create a new insight
      _ <- IO.println("Creating a new insight...")
      created <- repository.create(newInsight)
      _ <- IO.println(s"Created insight with ID: ${created.id.getOrElse("unknown")}")
      
      // Retrieve the created insight
      _ <- IO.println("\nRetrieving the created insight...")
      retrievedOpt <- repository.getById(created.id.get)
      _ <- retrievedOpt match {
        case Some(retrieved) => IO.println(s"Retrieved insight: $retrieved")
        case None => IO.println("Insight not found")
      }
      
      // Query by sensor ID
      _ <- IO.println("\nQuerying insights by sensor ID...")
      sensorInsights <- repository.getBySensorId(created.sensor)
      _ <- IO.println(s"Found ${sensorInsights.length} insights for sensor ${created.sensor}")
      
      // Query by building ID
      _ <- IO.println("\nQuerying insights by building ID...")
      buildingInsights <- repository.getByBuildingId(created.buildingId.get)
      _ <- IO.println(s"Found ${buildingInsights.length} insights for building ${created.buildingId.get}")
      
      // Query by date range
      _ <- IO.println("\nQuerying insights by date range...")
      dateInsights <- repository.getByDateRange(
        Instant.now().minusSeconds(3600), // 1 hour ago
        Instant.now()
      )
      _ <- IO.println(s"Found ${dateInsights.length} insights in the last hour")
      
      // Update the insight
      _ <- IO.println("\nUpdating the insight...")
      updated = created.copy(value = 23.0)
      updatedInsight <- repository.update(updated)
      _ <- IO.println(s"Updated value to: ${updatedInsight.value}")
      
      // Delete the insight
      _ <- IO.println("\nDeleting the insight...")
      _ <- repository.delete(updatedInsight.id.get)
      _ <- IO.println("Insight deleted")
      
    } yield ExitCode.Success
    
    program.handleErrorWith { error =>
      IO.println(s"An error occurred: ${error.getMessage}").as(ExitCode.Error)
    }
  }
}
