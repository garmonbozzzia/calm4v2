package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.headers.{Cookie, RawHeader}
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.util.ByteString
import wvlet.airframe.bind
import scala.collection.immutable.{Seq => ISeq}
import scala.concurrent.Future
import org.gbz.Global._
import org.gbz.Tag._

object Calm4WebClient {
  def uri[A: CalmUri](a:A): Uri = CalmUri[A].uri(a)
  def headers[A: CalmHeaders]: ISeq[HttpHeader] = CalmHeaders[A].headers
}

trait Calm4WebClient extends WebClient[CalmUri, CalmHeaders] {
  import Calm4WebClient._
  val auth = bind[AuthManager]
  override def get[T:CalmUri, R:CalmHeaders](calmRequest: T): Future[String @@ R] =
    for {
      auth <- auth.sessionId.map(Cookie("_sso_session", _))
      request = Get(uri(calmRequest)).withHeaders(auth +: headers)
      response <- Http().singleRequest(request)
      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield json.utf8String.@@[R]
}
