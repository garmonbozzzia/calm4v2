package calm.solid

import akka.http.scaladsl.Http
import org.gbz.Global.{materializer, system, _}
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.higherKinds

import org.gbz.Tag._

trait WebModule {
  trait HtmlSource[T]
  trait JsonSource[T]
  trait CourseListTag
  object AllCourses extends CourseListTag
}

object AuthDev extends
  TestSuite with
  WebModule with
  LogSupport with
  AuthMocModule {

  override implicit val authClient = mocAuthClient

  override def tests = Tests{
    'WebClient - {

    }
    'Storage{
      for{
        _ <- AuthManager[Int].sessionId
        _ <- AuthManager[Int].sessionId
        _ <- AuthManager[Int].sessionId
      } yield "Done"
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