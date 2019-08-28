package com.davidpuleri.cdnize.converters

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Tools {

  def normalise(input: String): String = {
    input.replaceAll("/", "-")
  }

  def time[R](block: => R): (R, Long) = {
    val t0 = System.nanoTime()
    val result = block // call-by-name
    val t1 = System.nanoTime()
    (result, (t1 - t0) / 100000)
  }
  def swap[T](o: Option[Future[T]]): Future[Option[T]] = o.map(_.map(Some(_))).getOrElse(Future.successful(None))

}



