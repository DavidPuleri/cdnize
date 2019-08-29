package com.davidpuleri.cdnize.services

import java.io.File
import java.nio.file.{Files, NoSuchFileException, Path, Paths}

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.davidpuleri.cdnize.converters.Tools
import com.davidpuleri.cdnize.converters.Tools._
import com.sksamuel.scrimage.nio.{GifWriter, ImageWriter, JpegWriter, PngWriter}
import com.sksamuel.scrimage.{Format, FormatDetector, Image}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PassthroughService(baseFolder: String, cacheFolder: String )(implicit val log: LoggingAdapter) extends Loggable {


  protected val fileRoute: Route =
    extractUnmatchedPath { uri =>
      parameter("forceRefresh".?, "width".?) { (forceRefresh, width) =>

        val chosenResourcePath = Paths.get(s"${baseFolder}${uri.toString()}")
        val hasBeenRefreshed: Future[Boolean] = {
          swap(forceRefresh.map(s => refreshCache(uri.toString(), width, cacheFolder))).map(_.getOrElse(false))
        }
        onSuccess(hasBeenRefreshed) { done =>
          width match {
            case Some(w) =>
              val width = w.toInt
              val cachedFilePath: Path = Paths.get(s"${cacheFolder}${width}${Tools.normalise(uri.toString())}")

              Files.exists(cachedFilePath) match {
                case true =>
                  log.debug(s"Loading file ${cachedFilePath.toString} from the cache")
                  getFromFile(cachedFilePath.toString)
                case false =>
                  Files.exists(chosenResourcePath) match {
                    case true =>
                      val image = Image.fromPath(chosenResourcePath)
                      val writer: ImageWriter = extractImageWriter(image)
                      val file = new File(cachedFilePath.toString)
                      log.debug(s"Loading file ${cachedFilePath.toString} from the filesystem")
                      val output = time {
                        image.scaleToWidth(width.toInt)
                          .output(file)(writer)
                      }
                      log.debug(s"Image resized in ${output._2}ms")
                      getFromFile(cachedFilePath.toString)

                    case false =>
                      complete(StatusCodes.NotFound)

                  }
              }

            case None =>
              getFromFile(chosenResourcePath.toString)
          }
        }
      }
    }

  private def refreshCache(uri: String, maybeWidth: Option[String], cacheFolder: String): Future[Boolean] = Future {
    val result = time {
      val value = s"$cacheFolder${maybeWidth.getOrElse("")}${normalise(uri)}"
      val path: Path = Paths.get(value)
      if (path.toFile.exists()) {
        try {
          Files.delete(path)
        } catch {
          case e: NoSuchFileException =>
          case _: Throwable =>

        }
      }
      true
    }

    log.debug(s"Cache cleared in ${result._2}ms")
    result._1
  }

  private def extractImageWriter(image: Image): ImageWriter = {

    val imageWriter = FormatDetector.detect(image.bytes) match {
      case Some(Format.GIF) =>
        GifWriter(true)
      case Some(Format.JPEG) =>
        JpegWriter(7, true)
      case Some(Format.PNG) =>
        PngWriter(7)
      case None =>
        throw new Throwable("Unsupported format to resize")
    }
    log.debug(s"Image Writer used is ${imageWriter}")
    imageWriter
  }



  val routes: Route = fileRoute


}
