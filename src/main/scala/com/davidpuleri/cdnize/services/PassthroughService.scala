package com.davidpuleri.cdnize.services

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.davidpuleri.cdnize.converters.Tools._
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.format.{Format, FormatDetector}
import com.sksamuel.scrimage.nio.{GifWriter, ImageWriter, JpegWriter, PngWriter}
import kamon.Kamon

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Path}
import java.util.Optional

class PassthroughService(baseFolder: String, cacheFolder: String)(implicit val log: LoggingAdapter) extends Loggable {


  protected val fileRoute: Route =
    extractUnmatchedPath { uri =>
      parameter("width".?) { (width) =>
        val sourceFile = FilePath(s"${baseFolder}${uri.toString()}").ifExist
        (sourceFile, width) match {
          case (Some(s), Some(w)) =>
            val cachedVersion = FilePath(s"${cacheFolder}${uri.toString()}").prefixFilename(s"width-$w")
            cachedVersion.exists() match {
              case true =>
                Kamon.counter("Loaded fom cache without resizing").withTag("Image","Cache Breakdown").increment()
                getFromFile(cachedVersion.file())
              case false =>
                Kamon.counter("Loaded fom cache after width resizing").withTag("Image","Cache Breakdown").increment()
                getFromFile(transformImage(s, cachedVersion, w.toInt))
            }

          case (None, None) =>
            Kamon.counter("Source image not found").withTag("Image","Cache Breakdown").increment()
            complete(StatusCodes.NotFound)
          case (Some(s), None) =>
            Kamon.counter("Not loaded from cache").withTag("Image","Cache Breakdown").increment()
            getFromFile(s.file())
        }
      }
    }

  private def transformImage(source: FilePath, destination: FilePath, width: Int): File = {
    val image: ImmutableImage = ImmutableImage.loader().fromPath(source.toPath())
    val writer: ImageWriter = extractImageWriter(source.path)
    Files.createDirectories(destination.toPath().getParent)
    val output = time {
      image.scaleToWidth(width).output(writer, destination.path)
    }
    log.debug(s"Image resized in ${output._2}ms")
    Kamon.histogram("Transform").withTag("Image","Transformation time").record(output._2)
    destination.file()
  }

  private def extractImageWriter(sourceFile: String): ImageWriter = {

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
      case None | Some(_) =>
        throw new Throwable("Unsupported format to resize")
    }
    log.debug(s"Image Writer used is ${imageWriter}")
    imageWriter
  }


  val routes: Route = fileRoute


}
