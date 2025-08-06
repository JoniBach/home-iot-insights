val scala3Version = "3.7.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "home-iot-insights",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.typelevel" %% "cats-core" % "2.13.0",         // core Cats library
      "org.typelevel" %% "cats-effect" % "3.6.3",        // Cats Effect for IO, Async, etc.
        // Start with this one
      "org.tpolecat" %% "doobie-core"      % "1.0.0-RC8",
      // And add any of these as needed
      "org.tpolecat" %% "doobie-h2"        % "1.0.0-RC8",          // H2 driver 1.4.200 + type mappings.
      "org.tpolecat" %% "doobie-hikari"    % "1.0.0-RC8",          // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres"  % "1.0.0-RC8",          // Postgres driver 42.7.5 + type mappings.
      "org.tpolecat" %% "doobie-specs2"    % "1.0.0-RC8" % "test", // Specs2 support for typechecking statements.
      "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC8" % "test",  // ScalaTest support for typechecking statements.
      "org.postgresql" % "postgresql" % "42.7.7",
      "com.amazonaws" % "aws-lambda-java-core" % "1.3.0",
"com.amazonaws" % "aws-lambda-java-events" % "3.16.1"

    )
  )
