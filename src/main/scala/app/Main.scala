
import cats.effect.{IO, IOApp}
import cats.implicits._
import infrastructure.db.repositories.DoobieReadingRepository
import application.usecases.GetLatestReadings

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    val repo = new DoobieReadingRepository
    val getLatestReadings = new GetLatestReadings(repo)

    val program = for {
      _ <- IO.println("Connecting to Supabase PostgreSQL database...")
      readings <- getLatestReadings.execute(10)
      _ <- IO.println(s"Found ${readings.length} readings:")
      _ <- IO.println("=" * 50)
      _ <- readings.traverse { reading =>
        IO.println(
          s"Reading ID: ${reading.id}, Device: ${reading.macAddress}, " +
          s"Temp: ${reading.temperature}°C, Humidity: ${reading.humidity}%, " +
          s"Pressure: ${reading.pressure} hPa, Time: ${reading.createdAt}"
        )
      }
    } yield ()

    program.handleErrorWith { error =>
      IO.println(s"Database connection failed: ${error.getMessage}")
    }
  }
}