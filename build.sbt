name := "Sample Users API using Pekko Http and Baklava"
version := "0.1.0-SNAPSHOT"
scalaVersion := "3.7.2"

enablePlugins(BaklavaSbtPlugin)
inConfig(Test)(
  BaklavaSbtPlugin.settings(Test) ++ Seq(
    fork := false,
    baklavaGenerateConfigs := Map(
      "openapi-info" ->
        s"""
          |{
          |  "openapi" : "3.0.1",
          |  "info" : {
          |    "title" : "${name.value}",
          |    "version" : "1.0.7"
          |  }
          |}
          |""".stripMargin
    )
  )
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-Wunused:all"
)

val pekkoV = "1.2.0"
val baklavaV = "1.0.7-1-329bc07-20250904T131837Z-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "users-pekko-api",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoV,
      "org.apache.pekko" %% "pekko-http-spray-json" % pekkoV,
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      "ch.qos.logback" % "logback-classic" % "1.4.14",
      "pl.iterators" %% "baklava-pekko-http-routes" % baklavaV,

      // Test dependencies
      "org.specs2" %% "specs2-core" % "4.20.5" % Test,
      "org.apache.pekko" %% "pekko-http-testkit" % pekkoV % Test,
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoV % Test,

      "pl.iterators" %% "baklava-pekko-http" % baklavaV % Test,
      "pl.iterators" %% "baklava-specs2" % baklavaV % Test,
      
      "pl.iterators" %% "baklava-openapi" % baklavaV % Test
      // "pl.iterators" %% "baklava-tsrest" % baklavaV % Test,
      // "pl.iterators" %% "baklava-simple" % baklavaV % Test
    )
  )
