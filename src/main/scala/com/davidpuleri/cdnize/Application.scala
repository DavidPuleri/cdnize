package com.davidpuleri.cdnize

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer
import com.davidpuleri.cdnize.config.AppConfig
import com.davidpuleri.cdnize.services.{HealthService, Loggable, PassthroughService}
import com.typesafe.config.{Config, ConfigFactory}
import kamon.Kamon

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object Application extends App with RouteConcatenation with Loggable {

  Kamon.init(ConfigFactory.load("kamon.conf"))

  private val maybeProvidedConfig: Option[Config] = Try(
    ConfigFactory.parseString(System.getenv("CDNIZE_CONFIG"))
  ).toOption

  private val maybeConfig: Config =
    maybeProvidedConfig.getOrElse{
      log.info("Unable to find configuration in environment variable CDNIZE_CONFIG, loading default")
      ConfigFactory.load()
    }

  implicit val system: ActorSystem = ActorSystem("cdnizei", maybeConfig)

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = system.log

  private val config = new AppConfig(maybeConfig)
  log.info(s"Application loaded with the following settings:")
  log.info(s"- Base folder: ${config.baseFolder}")
  log.info(s"- Cache folder: ${config.cacheFolder}")
  private val passthroughService = new PassthroughService(config.baseFolder, config.cacheFolder)
  val routes: Route = logAccessRequest {
    new HealthService().routes ~ passthroughService.routes
  }

  Http().bindAndHandle(routes, "0.0.0.0", config.port).onComplete {
    case Success(value) =>
      log.info(s"Server started on ${value.localAddress}")
    case Failure(exception) =>
      log.error(exception.getMessage)
  }

}
