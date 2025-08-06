package app.cli

import cats.effect.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import cats.implicits.*

import core.entities.Reading
import core.ports.ReadingRepository
import java.time.Instant

object DatabaseConfig {
  // Supabase PostgreSQL connection configuration
  val driver = "org.postgresql.Driver"
  
  // Read from environment variables with fallbacks for development
  val url = sys.env.getOrElse("DATABASE_URL", 
    throw new RuntimeException("DATABASE_URL environment variable not set"))
  val user = sys.env.getOrElse("DATABASE_USER", 
    throw new RuntimeException("DATABASE_USER environment variable not set"))
  val password = sys.env.getOrElse("DATABASE_PASSWORD", 
    throw new RuntimeException("DATABASE_PASSWORD environment variable not set"))

  val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    driver = driver,
    url = url,
    user = user,
    password = password,
    logHandler = None
  )
}

// Implementation of ReadingRepository trait using Doobie
object ReadingsRepository extends ReadingRepository[IO] {
  
  def getLatestReadings(limit: Int): IO[List[Reading]] = {
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure 
           FROM readings
           ORDER BY created_at DESC
           LIMIT $limit"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)
  }
  
  def getReadingsForPeriod(start: Instant, end: Instant): IO[List[Reading]] = {
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure 
           FROM readings 
           WHERE created_at >= $start AND created_at <= $end
           ORDER BY created_at DESC"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)
  }
  
  // Additional helper methods for backward compatibility
  def getAllReadings: IO[List[Reading]] = getLatestReadings(10)
  
  def getReadingsByDevice(macAddress: String): IO[List[Reading]] = {
    sql"""SELECT id, created_at, mac_address, temperature, humidity, pressure 
           FROM readings 
           WHERE mac_address = $macAddress
           ORDER BY created_at DESC
           LIMIT 10"""
      .query[Reading]
      .to[List]
      .transact(DatabaseConfig.transactor)
  }
}

object Example extends IOApp.Simple {
  def run: IO[Unit] = {
    val program: IO[Unit] = for {
      _ <- IO.println("Connecting to Supabase PostgreSQL database...")
      
      // Fetch latest readings using the implemented repository
      readings <- ReadingsRepository.getAllReadings
      _ <- IO.println(s"Found ${readings.length} readings:")
      _ <- readings.traverse(reading => IO.println(s"  Reading ID: ${reading.id}, Device: ${reading.macAddress}, Temp: ${reading.temperature}°C, Humidity: ${reading.humidity}%, Pressure: ${reading.pressure} hPa, Time: ${reading.createdAt}"))
      
      _ <- IO.println("\n" + "="*50)
      _ <- IO.println("Database connection successful!")
      
    } yield ()
    
    program.handleErrorWith { error =>
      IO.println(s"Database connection failed: ${error.getMessage}")
    }
  }
}
