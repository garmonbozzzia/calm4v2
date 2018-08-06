package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.Cookie
import org.gbz.Global.{materializer, system, _}
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.higherKinds
import org.gbz.Tag._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.util.ByteString


trait WebModule extends AuthCoreModule{
  trait HtmlContent[T]
  trait HtmlSource[T]{
    def html(t:T): Future[String@@HtmlContent[T]]
  }
  trait CalmUri[T]{
    def apply(t:T): Uri
  }
  def html[T,R](implicit calmUri: CalmUri[T], auth: AuthManager): HtmlSource[T] =
    calmRequest => for {
      auth <- auth.sessionId.map(Cookie("_sso_session", _))
      request = Get(calmUri(calmRequest)).withHeaders(auth)
      response <- Http().singleRequest(request)
      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield json.utf8String.@@[HtmlContent[T]]

  trait JsonSource[T]
}



object AuthDev extends TestSuite with WebModule with LogSupport with AuthMocModule {
  override implicit lazy val noStorageAuth: AuthManager@@NoStorage = mocAuthClient//.log

  override def tests = Tests{
    'WebClient - {

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
