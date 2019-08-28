package com.davidpuleri.cdnize.acceptance

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.davidpuleri.cdnize.services.PassthroughService
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.DurationInt

class PassthroughTest extends WordSpec with Matchers with ScalatestRouteTest  {

  implicit val log = system.log

  val baseUrl: String = getClass().getResource("/data").getPath
  val cacheFolder: String = getClass().getResource("/cache/").getPath
  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  "Passthrough service" should {
    "display file without altering" in {

      val service = new PassthroughService(baseUrl, cacheFolder)

      Get("/images/data-1/data-2/IMG_1499-1.JPG") ~> service.routes ~> check {
        status.intValue() shouldBe 200
      }
    }

    "returns not found if not exist" in {

      val service = new PassthroughService(baseUrl, cacheFolder)

      Get("/images/doesnt-exist") ~> Route.seal(service.routes) ~> check {
        status.intValue() shouldBe 404
      }
    }


    "display file in smaller size" in {

      val service = new PassthroughService(baseUrl, cacheFolder)

      Get("/images/data-1/data-2/IMG_1499-1.JPG?width=700") ~> service.routes ~> check {
        status.intValue() shouldBe 200
      }
    }

    "force refresh one image" in {

      val service = new PassthroughService(baseUrl, cacheFolder)

      Get("/images/data-1/data-2/IMG_1499-2.JPG?width=700&forceRefresh=1") ~> service.routes ~> check {
        status.intValue() shouldBe 200
      }
    }
  }
}
