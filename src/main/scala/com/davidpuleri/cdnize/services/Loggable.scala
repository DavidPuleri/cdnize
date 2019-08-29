package com.davidpuleri.cdnize.services

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.{Directive0, RouteResult}
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LoggingMagnet}

trait Loggable {

  val log: LoggingAdapter


  private def doLogging: LoggingAdapter => HttpRequest => RouteResult => Unit = {
    (log: LoggingAdapter) =>
      (httpRequest: HttpRequest) => {
        routeResult: RouteResult => {
          routeResult match {
            case Complete(httpResponse) =>
              log.info(s"[${httpRequest.uri.path}] [${httpRequest.method.value}] [${httpResponse.status.intValue()}]")
            case Rejected(rejections) =>
              log.info(s"[${httpRequest.uri.path}] [${httpRequest.method.value}] [404]")
          }
        }
      }
  }


  def logAccessRequest: Directive0 = DebuggingDirectives.logRequestResult(LoggingMagnet(doLogging))

}
