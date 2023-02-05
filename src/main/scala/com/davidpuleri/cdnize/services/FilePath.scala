package com.davidpuleri.cdnize.services

import java.io.File
import java.nio.file.Paths

case class FilePath(path: String) {

  def file(): File = Paths.get(path).toFile

  def prefixFilename(prefix: String) = {

    val begining = path(0) match {
      case '/' => "/"
      case _ => ""
    }

    val folders = path.split("/").filterNot(_.isEmpty)
    val filename = folders.last
    FilePath(s"$begining${folders.dropRight(1).mkString("/")}/$prefix-${filename}".trim)
  }

  def toPath() = file().toPath

  def exists(): Boolean = file().exists()

  def ifExist = {
    if(exists()) {
      Some(FilePath(path))
    } else None
  }

}