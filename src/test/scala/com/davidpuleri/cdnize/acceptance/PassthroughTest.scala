package com.davidpuleri.cdnize.acceptance

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.davidpuleri.cdnize.services.PassthroughService
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.DurationInt

class PassthroughTest extends WordSpec with Matchers with ScalatestRouteTest  {

  implicit val log = system.log

  val baseUrl = "target/scala-2.12/test-classes/data/"
  val cacheFolder = "target/scala-2.12/test-classes/cache/"


  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(60.seconds)

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
      java.nio.file.Paths.get("target/scala-2.12/test-classes/cache/images/data-1/data-2/width-700-IMG_1499-1.JPG").toFile.delete()
      Get("/images/data-1/data-2/IMG_1499-1.JPG?width=700") ~> service.routes ~> check {
        status.intValue() shouldBe 200
      }
      java.nio.file.Paths.get("target/scala-2.12/test-classes/cache/images/data-1/data-2/width-700-IMG_1499-1.JPG").toFile.exists() shouldBe true
      Get("/images/data-1/data-2/IMG_1499-1.JPG?width=700") ~> service.routes ~> check {
        status.intValue() shouldBe 200
      }
    }

    "display file in smaller size by height" in {

      val service = new PassthroughService(baseUrl, cacheFolder)
      java.nio.file.Paths.get("target/scala-2.12/test-classes/cache/images/data-1/data-2/height-700-IMG_1499-3.JPG").toFile.delete()
      Get("/images/data-1/data-2/IMG_1499-3.JPG?height=700") ~> service.routes ~> check {
        status.intValue() shouldBe 200
      }
      java.nio.file.Paths.get("target/scala-2.12/test-classes/cache/images/data-1/data-2/height-700-IMG_1499-3.JPG").toFile.exists() shouldBe true
      Get("/images/data-1/data-2/IMG_1499-3.JPG?height=700") ~> service.routes ~> check {
        status.intValue() shouldBe 200
      }
    }

    "force refresh one image" in {

      java.nio.file.Paths.get("target/scala-2.12/test-classes/cache/images/data-1/data-2/IMG_1499-2.JPG").toFile.delete()
      val service = new PassthroughService(baseUrl, cacheFolder)

      Get("/images/data-1/data-2/IMG_1499-2.JPG?width=700&forceRefresh=1") ~> service.routes ~> check {
        status.intValue() shouldBe 200
        java.nio.file.Paths.get("target/scala-2.12/test-classes/cache/images/data-1/data-2/width-700-IMG_1499-2.JPG").toFile.exists() shouldBe true
      }

      Get("/images/data-1/data-2/IMG_1499-2.JPG?width=700&forceRefresh=1") ~> service.routes ~> check {
        status.intValue() shouldBe 200
        java.nio.file.Paths.get("target/scala-2.12/test-classes/cache/images/data-1/data-2/width-700-IMG_1499-2.JPG").toFile.exists() shouldBe true
      }
    }
  }
}
