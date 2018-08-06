package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Path
import org.gbz.Global.{materializer, system, _}
import org.gbz.Tag._
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.{higherKinds, implicitConversions}

trait MocWebModule extends WebModule {
  override implicit def htmlSource[T: CalmUri](implicit auth: AuthManager @@ Default) = super.htmlSource.log
}

object AuthDev extends
  TestSuite with
  MocWebModule with
  LogSupport with
  AuthMocModule {
//  override implicit lazy val noStorageAuth: AuthManager@@NoStorage = mocAuthClient//.log
  val host = Uri("https://calm.dhamma.org")
  implicit def seq2query(seq: Seq[(String, String)]): Uri.Query = Uri.Query(seq.toMap)
  implicit def string2Path(str: String): Path = Path(str)

  override def tests = Tests{
    'WebClient - {
      implicit val mocUri: CalmUri[Int] = id => host.withPath(s"/en/courses/$id/course_applications")
//      for{
//        htmlPage <- html(4053)
//        jsonPage <- json(4053)
//      } yield "Done"
//        .log(htmlPage.substring(0,100))
//        .log(jsonPage.substring(0,100))
//        .logWarn("MSG")
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
