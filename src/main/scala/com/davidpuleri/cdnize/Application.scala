package com.davidpuleri.cdnize

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer
import com.davidpuleri.cdnize.config.AppConfig
import com.davidpuleri.cdnize.services.{HealthService, Loggable, PassthroughService}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Application extends App with RouteConcatenation with Loggable {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val log: LoggingAdapter = system.log

  private val config = new AppConfig(ConfigFactory.load("application.json"))
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
