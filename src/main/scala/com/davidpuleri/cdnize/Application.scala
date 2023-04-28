package com.davidpuleri.cdnize

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.davidpuleri.cdnize.config.AppConfig
import com.davidpuleri.cdnize.services.{HealthService, Loggable, PassthroughService}
import com.typesafe.config.{Config, ConfigFactory}
import kamon.Kamon
import kamon.instrumentation.akka.http.ServerFlowWrapper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object Application extends App with RouteConcatenation with Loggable {

  Kamon.init(ConfigFactory.load("kamon.conf"))

  private def maybeProvidedConfig: Option[Config] = Try(
    ConfigFactory.parseString(System.getenv("CDNIZE_CONFIG"))
  ).toOption

  implicit private def config: Config =
    maybeProvidedConfig.getOrElse{
     println("Unable to find configuration in environment variable CDNIZE_CONFIG, loading default")
      ConfigFactory.load()
    }

  implicit val system: ActorSystem = ActorSystem("cdnizei", config)

  implicit val log: LoggingAdapter = system.log

  private val applicationConfig = new AppConfig(config)
  log.info(s"Application loaded with the following settings:")
  log.info(s"- Base folder: ${applicationConfig.baseFolder}")
  log.info(s"- Cache folder: ${applicationConfig.cacheFolder}")
  private val passthroughService = new PassthroughService(applicationConfig.baseFolder, applicationConfig.cacheFolder)



  val routes: Route = logAccessRequest {
    new HealthService().routes ~ passthroughService.routes
  }

  val flow= ServerFlowWrapper(routes, "0.0.0.0", applicationConfig.port)
  Http().newServerAt("0.0.0.0", applicationConfig.port).bindFlow(flow).onComplete {
    case Success(value) =>
      log.info(s"Server started on ${value.localAddress}")
    case Failure(exception) =>
      log.error(exception.getMessage)
  }

}

