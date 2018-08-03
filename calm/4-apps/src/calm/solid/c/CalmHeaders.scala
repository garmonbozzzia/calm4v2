package calm.solid

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader
import scala.collection.immutable.{Seq => ISeq}

trait CalmHeaders[+T]{
  def headers: ISeq[HttpHeader]
}

object CalmHeaders{
  def apply[A](implicit request: CalmHeaders[A]): CalmHeaders[A] = request
  def pure[A](hs: ISeq[HttpHeader]): CalmHeaders[A] = new CalmHeaders[A] {
    override def headers: ISeq[HttpHeader] = hs
  }
  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")
  val xmlHeaders: ISeq[HttpHeader] = scala.collection.immutable.Seq(accept,xml,referer)
  implicit def html[T]: CalmHeaders[HtmlSource[T]] = pure(ISeq.empty)
  implicit def json[T]: CalmHeaders[JsonSource[T]] = pure(xmlHeaders)
}
