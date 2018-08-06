package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.http.scaladsl.model.headers.{Cookie, RawHeader}
import akka.util.ByteString
import scala.collection.immutable.{Seq => ISeq}
import scala.concurrent.Future
import org.gbz.Tag._
import org.gbz.Global._

trait WebModule extends WebCoreModule with AuthCoreModule{
  trait CalmUri[T] extends Apply[CalmUri,T,Uri]

  def xmlHeaders: ISeq[HttpHeader] = scala.collection.immutable.Seq(
    RawHeader("Accept", "application/json, text/javascript, */*; q=0.01"),
    RawHeader("X-Requested-With", "XmlHttpRequest"),
    RawHeader("Referer", "")
  )

  implicit def htmlSource[T:CalmUri](implicit auth: AuthManager@@Default): HtmlSource[T] =
    content(_,ISeq.empty)

  implicit def jsonSource[T:CalmUri](implicit auth: AuthManager@@Default): JsonSource[T] =
    content(_,xmlHeaders)

  implicit def content[A,B](a:A, headers: ISeq[HttpHeader])(
    implicit uri: CalmUri[A], auth: AuthManager@@Default): Future[String@@B] =
    for {
      auth <- auth.sessionId.map(Cookie("_sso_session", _))
      request = Get(uri(a)).withHeaders(auth +: headers)
      response <- Http().singleRequest(request)
      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield json.utf8String.@@[B]
}
