package calm.solid

import akka.http.scaladsl.Http
import utest._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.{higherKinds, implicitConversions}
import org.gbz.Global.{materializer, system, _}
import org.gbz.Tag._
import org.gbz.utils.log.Log._

trait TestModule extends AppModule with LogSupport {
  def mocAuthClient: AuthManager@@NoStorage =
    AuthManager.pure(Future.successful("<SessionId>".@@[SessionIdTag])).@@[NoStorage]
}

object AuthDev extends TestSuite with TestModule
{
  override implicit lazy val noStorageAuth: AuthManager@@NoStorage = mocAuthClient//.log

  override def tests = Tests{
    'Uri - {
      uri("Игнатьев".@@[SearchTag])
      uri[AppRequest](123.@@[AppIdTag] -> 4053.@@[CourseIdTag]).log
      uri[CourseListRequest](().taggedWith[CourseListRequestTag]).log
//      uri[AppRequest](123 -> 4053)
//      val bbb: Option[Int@@String] = Some(1)
//      val bbb: Option[Int@@String] = toTaggedOption(Some(1))
    }

    'WebClient - {
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

trait AuthMocModule {
  this: AuthModule with AuthCoreModule with AuthEntitiesModule =>
}
