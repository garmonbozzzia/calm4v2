package org.gbz.calm.model

import akka.http.scaladsl.model.{HttpHeader, Uri}

trait CalmRequest[T]{
  def uri: Uri
  def parseEntity(data: String): T
  def headers: scala.collection.immutable.Seq[HttpHeader] = Nil
}