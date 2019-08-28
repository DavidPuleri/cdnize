package com.davidpuleri.cdnize.converters

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class ToolsTest  extends WordSpec with Matchers with ScalatestRouteTest {

  "Tools" should {
    "normalise" in {

      Tools.normalise("images/live/Sporting-live/OGCNSCP-4.jpeg") shouldBe ("images-live-Sporting-live-OGCNSCP-4.jpeg")
    }
  }
}
