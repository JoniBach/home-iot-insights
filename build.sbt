val scala3Version = "3.7.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "home-iot-insights",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,

    // Dependencies
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.typelevel" %% "cats-core" % "2.13.0",
      "org.typelevel" %% "cats-effect" % "3.6.3",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC8",
      "org.tpolecat" %% "doobie-h2" % "1.0.0-RC8",
      "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC8",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC8",
      "org.tpolecat" %% "doobie-specs2" % "1.0.0-RC8" % Test,
      "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC8" % Test,
      "org.postgresql" % "postgresql" % "42.7.7",
      "com.amazonaws" % "aws-lambda-java-core" % "1.3.0",
      "com.amazonaws" % "aws-lambda-java-events" % "3.16.1",
    ),

    // Assembly settings
    assembly / assemblyJarName := "insights-service.jar",
    assembly / mainClass := Some("app.InsightsLambdaHandler"), // we'll make this
    assembly / assemblyMergeStrategy := {
      case PathList("module-info.class") => MergeStrategy.discard
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    Test / parallelExecution := false
  )
