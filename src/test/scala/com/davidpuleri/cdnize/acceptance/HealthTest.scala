package com.davidpuleri.cdnize.acceptance

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.davidpuleri.cdnize.services.HealthService
import org.scalatest.{Matchers, WordSpec}


class HealthTest extends WordSpec with Matchers with ScalatestRouteTest {

  "HealthTest" should {

    "Return a valid health page" in {

      Get("/health") ~> new HealthService().routes ~> check {
        status.intValue() shouldBe 200
      }

    }

  }


}
