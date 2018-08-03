package calm.solid

import akka.http.scaladsl.Http
import utest._
import scala.concurrent.{Await, Future}
import scala.util.Try
import org.gbz.Global._
import org.gbz.Tag._
import org.gbz.utils.log.Log._
import Types._
import ammonite.ops.{write => writeFile, read => readFile, _}

class MocAuthClient extends AuthClient with LogSupport {
  var id = 0
  override def signIn: Future[SessionId] = {
    id += 1
    Future(id.toString.@@[SessionIdTag])
  }
}

class MocSessionStorage extends Storage[SessionId] {
  override def write(obj: SessionId): Unit = {}
  override def read[U](key: U) = None
}

import org.gbz.Tag._
class MocFileSessionStorage extends Storage[SessionId] {
  val filepath = pwd/'storage/'session
  override def write(obj: SessionId): Unit = writeFile(filepath, obj)
  override def read[U](key: U): Option[SessionId] =
    Try(readFile(filepath): SessionId).toOption
}

object AuthTest extends TestSuite with LogSupport with Designs {
  override def tests = Tests {
    'Auth - {
      val manager = mainDesign
//        .bind[Credentials].toInstance(Credentials("login", "password", ""))
//        .bind[Storage[SessionId]].to[MocSessionStorage]
        .bind[Storage[SessionId]].to[MocFileSessionStorage]
//        .bind[AuthClient].to[MocAuthClient]
        .newSession.build[AuthManager]
      for {
        id1 <- manager.sessionId
        id2 <- manager.sessionId
      } yield id1 ==> id2.logInfo
    }

    'GetCourseList {
//      val webClient: WebClient = mainDesign.newSession.build[WebClient]

//      implicit val a: HeaderHelper[CourseList] = defaultHeaderHelper[CourseList]
//      webClient.get(CourseList)
    }
  }

  override def utestAfterAll(): Unit = {
    super.utestAfterAll()
    import scala.concurrent.duration._
    Http().shutdownAllConnectionPools().andThen { case _ =>
      materializer.shutdown()
      system.terminate()
    }
    Await.ready(system.terminate(),10 seconds)
  }
}
