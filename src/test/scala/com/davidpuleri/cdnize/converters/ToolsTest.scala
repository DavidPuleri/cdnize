package com.davidpuleri.cdnize.converters

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.davidpuleri.cdnize.services.FilePath
import org.scalatest.{Matchers, WordSpec}

class ToolsTest extends WordSpec with Matchers with ScalatestRouteTest {

  "Tools" should {
    "normalise" in {

      Tools.normalise("images/live/Sporting-live/OGCNSCP-4.jpeg") shouldBe ("images-live-Sporting-live-OGCNSCP-4.jpeg")
    }

    "Transform source path to cached path" in {
      FilePath("/home/david/basefolder/folder1-folder-2/folder-3/filename.jpg").prefixFilename("width-300") shouldBe FilePath("/home/david/basefolder/folder1-folder-2/folder-3/width-300-filename.jpg")
    }
  }
}


