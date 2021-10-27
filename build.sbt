import Dependencies._

organization := "davidpuleri"

name := "cdnize"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.23"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.4",
  "com.typesafe.akka" %% "akka-http" % "10.1.8",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.8",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.8",
  "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.8",
  "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.8",
  "org.slf4j"              % "slf4j-api"            % "1.7.30",
  "ch.qos.logback"         % "logback-classic"      % "1.2.6",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "org.scalactic" %% "scalactic" % "3.0.8",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

libraryDependencies ++= Kamon.all
enablePlugins(DockerPlugin)
val versionNumber = settingKey[String]("The application version")

versionNumber := {
  IO.readLines(new File("VERSION")).mkString
}

imageNames in docker := Seq(
  // Sets the latest tag
  ImageName(s"docker-registry.ekinox.design/${organization.value}/${name.value}:latest"),

  // Sets a name with a tag that contains the project version
  ImageName(
    namespace = Some("ekinox"),
    repository = name.value,
    tag = Some(versionNumber.value)
  )
)

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:11-jre")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
    volume("/data/base")
    volume("/data/cache")
    expose(80)
  }
}

buildOptions in docker := BuildOptions(cache = false)
