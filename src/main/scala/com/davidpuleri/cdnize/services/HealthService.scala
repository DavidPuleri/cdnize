package com.davidpuleri.cdnize.services

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class HealthService {

  val routes: Route = path("health") {
    complete("OK")
  }
}
