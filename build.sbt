// see http://www.scala-sbt.org/0.13/docs/Parallel-Execution.html for details
concurrentRestrictions in Global := Seq(
  Tags.limit(Tags.Test, 1)
)

lazy val commonSettings = Seq(
  scalaVersion := "2.11.12",
  scalacOptions ++= Seq("-feature"),
  organization := "com.ubirch.user",

  homepage := Some(url("http://ubirch.com")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/ubirch/ubirch-user-service"),
    "scm:git:git@github.com:ubirch/ubirch-user-service.git"
  )),
  version := "1.0.1-SNAPSHOT",
  test in assembly := {},
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

)

/*
 * MODULES
 ********************************************************/

lazy val userService = (project in file("."))
  .settings(
    commonSettings,
    publishArtifact := false
  )
  .aggregate(
    clientRest,
    cmdtools,
    config,
    core,
    modelDb,
    modelDbTools,
    modelRest,
    server,
    testTools,
    testToolsExt,
    util
  )

lazy val clientRest = (project in file("client-rest"))
  .settings(commonSettings: _*)
  .dependsOn(config, modelRest, util, core % "test")
  .settings(
    name := "client-rest",
    description := "REST client of the user-service",
    libraryDependencies ++= depClientRest
  )

lazy val cmdtools = project
  .settings(commonSettings: _*)
  .dependsOn(modelDbTools, testToolsExt)
  .settings(
    description := "command line tools"
  )

lazy val config = project
  .settings(commonSettings: _*)
  .settings(
    description := "user-service specific config and config tools",
    libraryDependencies += ubirchConfig
  )

lazy val core = project
  .settings(commonSettings: _*)
  .dependsOn(config, modelDb, modelDbTools, modelRest, util, testTools % "test")
  .settings(
    description := "business logic",
    libraryDependencies ++= depCore
  )

lazy val modelDb = (project in file("model-db"))
  .settings(commonSettings: _*)
  .settings(
    name := "model-db",
    description := "database JSON models",
    libraryDependencies ++= depModelDb
  )

lazy val modelDbTools = (project in file("model-db-tools"))
  .settings(commonSettings: _*)
  .dependsOn(modelDb)
  .settings(
    name := "model-db-tools",
    description := "tools for database JSON models",
    libraryDependencies ++= depModelDb
  )

lazy val modelRest = (project in file("model-rest"))
  .settings(commonSettings: _*)
  .settings(
    name := "model-rest",
    description := "REST JSON models"
  )

lazy val server = project
  .settings(commonSettings: _*)
  .settings(mergeStrategy: _*)
  .dependsOn(config, core, modelRest, util)
  .enablePlugins(DockerPlugin)
  .settings(
    description := "REST interface and Akka HTTP specific code",
    libraryDependencies ++= depServer,
    fork in run := true,
    resolvers ++= Seq(
      resolverSeebergerJson
    ),
    mainClass in(Compile, run) := Some("com.ubirch.user.server.Boot"),
    resourceGenerators in Compile += Def.task {
      generateDockerFile(baseDirectory.value / ".." / "Dockerfile.input", (assemblyOutputPath in assembly).value)
    }.taskValue
  )

lazy val testTools = (project in file("test-tools"))
  .settings(commonSettings: _*)
  .dependsOn(config, modelDb)
  .settings(
    name := "test-tools",
    description := "tools useful in automated tests",
    libraryDependencies ++= depTestTools
  )

lazy val testToolsExt = (project in file("test-tools-ext"))
  .settings(commonSettings: _*)
  .dependsOn(core, modelDb)
  .settings(
    name := "test-tools-ext",
    description := "tools useful in automated tests (not in test-tools to avoid circular dependencies between _test-tools_ and _core_)",
    libraryDependencies ++= depTestTools
  )

lazy val util = project
  .settings(commonSettings: _*)
  .settings(
    description := "utils",
    libraryDependencies ++= depUtils
  )

/*
 * MODULE DEPENDENCIES
 ********************************************************/

lazy val depClientRest = Seq(
  akkaHttp,
  akkaStream,
  json4sNative,
  ubirchResponse,
  ubirchDeepCheckModel
) ++ scalaLogging

lazy val depServer = Seq(

  akkaSlf4j,
  akkaHttp,
  akkaStream,
  ubirchRestAkkaHttp,
  ubirchResponse,

  ubirchRestAkkaHttpTest % "test"

)

lazy val depCore = Seq(
  akkaActor,
  ubirchDeepCheckModel,
  json4sNative,
  ubirchJson,
  ubirchMongo,
  ubirchCrypto,
  ubirchResponse,
  scalatest % "test"
) ++ scalaLogging

lazy val depModelDb = Seq(
  ubirchUuid,
  ubirchDate
)

lazy val depTestTools = Seq(
  json4sNative,
  ubirchJson,
  scalatest,
  ubirchMongo,
  ubirchMongoTest
) ++ scalaLogging

lazy val depUtils = Seq(
)

/*
 * DEPENDENCIES
 ********************************************************/

// VERSIONS
val akkaV = "2.5.11"
val akkaHttpV = "10.1.3"
val json4sV = "3.6.0"

val scalaTestV = "3.0.1"

lazy val logbackV = "1.2.3"
lazy val logbackESV = "1.5"
lazy val slf4jV = "1.7.25"
lazy val log4jV = "2.9.1"
lazy val scalaLogV = "3.7.2"
lazy val scalaLogSLF4JV = "2.1.2"


// GROUP NAMES
val ubirchUtilG = "com.ubirch.util"
val json4sG = "org.json4s"
val akkaG = "com.typesafe.akka"
val typesafePlayG = "com.typesafe.play"
val slf4jG = "org.slf4j"
val typesafeLoggingG = "com.typesafe.scala-logging"
val logbackG = "ch.qos.logback"

lazy val scalatest = "org.scalatest" %% "scalatest" % scalaTestV

lazy val json4sNative = json4sG %% "json4s-native" % json4sV

lazy val scalaLogging = Seq(
  slf4jG % "slf4j-api" % slf4jV,
  slf4jG % "log4j-over-slf4j" % slf4jV,
  slf4jG % "jul-to-slf4j" % slf4jV,
  logbackG % "logback-core" % logbackV,
  logbackG % "logback-classic" % logbackV,
  "net.logstash.logback" % "logstash-logback-encoder" % "5.0",
  typesafeLoggingG %% "scala-logging-slf4j" % scalaLogSLF4JV,
  typesafeLoggingG %% "scala-logging" % scalaLogV
)

lazy val akkaActor = akkaG %% "akka-actor" % akkaV
lazy val akkaHttp = akkaG %% "akka-http" % akkaHttpV
lazy val akkaSlf4j = akkaG %% "akka-slf4j" % akkaV
lazy val akkaStream = akkaG %% "akka-stream" % akkaV

lazy val excludedLoggers = Seq(
  ExclusionRule(organization = typesafeLoggingG),
  ExclusionRule(organization = slf4jG),
  ExclusionRule(organization = logbackG)
)

lazy val ubirchConfig = ubirchUtilG %% "config" % "0.2.3" excludeAll (excludedLoggers: _*)
lazy val ubirchCrypto = ubirchUtilG %% "crypto" % "0.4.11" excludeAll (excludedLoggers: _*)
lazy val ubirchDate = ubirchUtilG %% "date" % "0.5.3" excludeAll (excludedLoggers: _*)
lazy val ubirchDeepCheckModel = ubirchUtilG %% "deep-check-model" % "0.3.0" excludeAll (excludedLoggers: _*)
lazy val ubirchJson = ubirchUtilG %% "json" % "0.5.1" excludeAll (excludedLoggers: _*)
lazy val ubirchMongo = ubirchUtilG %% "mongo-utils" % "0.8.3" excludeAll (excludedLoggers: _*)
lazy val ubirchMongoTest = ubirchUtilG %% "mongo-test-utils" % "0.8.3" excludeAll (excludedLoggers: _*)
lazy val ubirchResponse = ubirchUtilG %% "response-util" % "0.4.0" excludeAll (excludedLoggers: _*)
lazy val ubirchRestAkkaHttp = ubirchUtilG %% "rest-akka-http" % "0.4.0" excludeAll (excludedLoggers: _*)
lazy val ubirchRestAkkaHttpTest = ubirchUtilG %% "rest-akka-http-test" % "0.4.0" excludeAll (excludedLoggers: _*)
lazy val ubirchUuid = ubirchUtilG %% "uuid" % "0.1.3" excludeAll (excludedLoggers: _*)

/*
 * RESOLVER
 ********************************************************/

lazy val resolverSeebergerJson = Resolver.bintrayRepo("hseeberger", "maven")

/*
 * MISC
 ********************************************************/

lazy val mergeStrategy = Seq(
  assemblyMergeStrategy in assembly := {
    case PathList("org", "joda", "time", xs@_*) => MergeStrategy.first
    case m if m.toLowerCase.endsWith("manifest.mf") => MergeStrategy.discard
    case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
    case m if m.toLowerCase.endsWith("application.conf") => MergeStrategy.concat
    case m if m.toLowerCase.endsWith("application.dev.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("application.base.conf") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("logback.xml") => MergeStrategy.first
    case m if m.toLowerCase.endsWith("logback-test.xml") => MergeStrategy.discard
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)

def generateDockerFile(file: File, jarFile: sbt.File): Seq[File] = {
  val contents =
    s"""SOURCE=server/target/scala-2.11/${jarFile.getName}
       |TARGET=${jarFile.getName}
       |""".stripMargin
  IO.write(file, contents)
  Seq(file)
}
