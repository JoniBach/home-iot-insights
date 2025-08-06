package infrastructure.db.config

import cats.effect.IO
import doobie.util.transactor.Transactor

object DatabaseConfig {
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
