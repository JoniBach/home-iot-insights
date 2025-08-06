package infrastructure.db.config

import cats.effect.IO
import doobie.util.transactor.Transactor

object DatabaseConfig {
  val driver = "org.postgresql.Driver"
  
    val host = sys.env.getOrElse("HOST", 
      throw new RuntimeException("HOST environment variable not set"))
    val port = sys.env.getOrElse("PORT", 
      throw new RuntimeException("PORT environment variable not set"))
    val database = sys.env.getOrElse("DATABASE", 
      throw new RuntimeException("DATABASE environment variable not set"))
    val user = sys.env.getOrElse("USER", 
      throw new RuntimeException("USER environment variable not set"))
    val poolMode = sys.env.getOrElse("POOL_MODE", 
      throw new RuntimeException("POOL_MODE environment variable not set"))

    val url = s"jdbc:postgresql://$host:$port/$database?sslmode=require"
    val password = sys.env.getOrElse("PASSWORD", 
      throw new RuntimeException("PASSWORD environment variable not set"))


  Class.forName(driver)

  val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    driver = driver,
    url = url,
    user = user,
    password = password,
    logHandler = None
  )
}
