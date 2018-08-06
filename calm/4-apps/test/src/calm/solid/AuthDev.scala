package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.http.scaladsl.model.headers.{Cookie, RawHeader}
import org.gbz.Global.{materializer, system, _}
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.{higherKinds, implicitConversions}
import org.gbz.Tag._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.Uri.Path
import akka.util.ByteString

trait WebCoreModule extends WebEntityModel with AuthCoreModule{

  def html[A:HtmlSource](t:A): Future[String @@ HtmlContent[A]] = HtmlSource[A](t)
  def json[A:JsonSource](t:A): Future[String @@ JsonContent[A]] = JsonSource[A](t)

  trait HtmlSource[T] extends Apply[HtmlSource,T,Future[String@@HtmlContent[T]]]
  trait JsonSource[T] extends Apply[JsonSource,T,Future[String@@JsonContent[T]]]

  object JsonSource {
    def apply[A](a:A)(implicit v: JsonSource[A] ): Future[String @@ JsonContent[A]] = v(a)
  }

  object HtmlSource {
//    def apply[A](a:A)(implicit v: HtmlSource[A] ): Future[String @@ HtmlContent[A]] = v(a)
    def apply[A](a:A)(implicit v: HtmlSource[A] ): Future[String @@ HtmlContent[A]] = v(a)
  }
}

trait WebModule extends WebCoreModule with AuthCoreModule{
  import scala.collection.immutable.{Seq => ISeq}
  trait CalmUri[T] extends Apply[CalmUri,T,Uri]

  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")
  def xmlHeaders: ISeq[HttpHeader] = scala.collection.immutable.Seq(accept,xml,referer)

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

trait MocWebModule extends WebModule {
  override implicit def htmlSource[T: CalmUri](implicit auth: AuthManager @@ Default) = super.htmlSource.log
}

object AuthDev extends TestSuite with MocWebModule with LogSupport with AuthMocModule {
//  override implicit lazy val noStorageAuth: AuthManager@@NoStorage = mocAuthClient//.log
  val host = Uri("https://calm.dhamma.org")
  implicit def seq2query(seq: Seq[(String, String)]): Uri.Query = Uri.Query(seq.toMap)
  implicit def string2Path(str: String): Path = Path(str)

  override def tests = Tests{
    'WebClient - {
      implicit val mocUri: CalmUri[Int] = id => host.withPath(s"/en/courses/$id/course_applications")
      for{
        htmlPage <- html(4053)
        jsonPage <- json(4053)
      } yield "Done"
        .log(htmlPage.substring(0,100))
        .log(jsonPage.substring(0,100))
        .logWarn("MSG")
      //todo выяснить почему сообщения печатаются в неправильном порядке
    }
    'Storage{
      for{
        _ <- sessionId
        _ <- sessionId
        res <- sessionId
      } yield res
    }
  }

  override def utestAfterAll(): Unit = {
    super.utestAfterAll()
    Http().shutdownAllConnectionPools().andThen { case _ =>
      materializer.shutdown()
      system.terminate()
    }
    Await.ready(system.terminate(),10 seconds)
  }
}

trait AuthMocModule extends AuthModule {
  def mocAuthClient: AuthManager@@NoStorage =
    AuthManager.pure(Future.successful("<SessionId>".@@[SessionIdTag])).taggedWith[NoStorage]
}
