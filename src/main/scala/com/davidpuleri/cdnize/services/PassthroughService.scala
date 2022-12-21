package com.davidpuleri.cdnize.services

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.davidpuleri.cdnize.converters.Tools
import com.davidpuleri.cdnize.converters.Tools._
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.format.{Format, FormatDetector}
import com.sksamuel.scrimage.nio.{GifWriter, ImageWriter, JpegWriter, PngWriter}

import java.io.{File, FileInputStream}
import java.nio.file.{Files, NoSuchFileException, Path, Paths}
import java.util.Optional
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
                      val image1: ImmutableImage = ImmutableImage.loader().fromPath(chosenResourcePath)

                      val writer: ImageWriter = extractImageWriter(image1,chosenResourcePath.toString)
                      val file = new File(cachedFilePath.toString)
                      log.debug(s"Loading file ${cachedFilePath.toString} from the filesystem")
                      val output = time {
                        image1.scaleToWidth(width.toInt)
                          .output(writer, file)
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

  private def extractImageWriter(image: ImmutableImage, sourceFile: String): ImageWriter = {

    def toScalaOption[A](maybeA: Optional[A]): Option[A] =
      if (maybeA.isEmpty) None else Some(maybeA.get)

    val value: Option[Format] = toScalaOption(FormatDetector.detect(new FileInputStream(sourceFile)))
    val imageWriter = value match {
      case Some(Format.GIF) =>
        GifWriter.Default
      case Some(Format.JPEG) =>
        new JpegWriter().withCompression(70).withProgressive(true)
      case Some(Format.PNG) =>
        new PngWriter().withCompression(7)
      case None =>
        throw new Throwable("Unsupported format to resize")
    }
    log.debug(s"Image Writer used is ${imageWriter}")
    imageWriter
  }



  val routes: Route = fileRoute


}
