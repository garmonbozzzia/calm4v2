package calm

import org.gbz.Global._
import org.gbz.utils.log.Log._

import akka.http.scaladsl.Http
import utest._
import wvlet.airframe._
import wvlet.surface.tag._
import scala.concurrent.{Await, Future}

class MocAuthClient extends AuthClient with LogSupport {
  var id = 0
  override def signIn: Future[String @@ SessionId] = {
    id += 1
    Future(id.toString.taggedWith[SessionId])
  }
}

class MocSessionStorage extends Storage[String@@SessionId] {
  override def write(obj: String @@ SessionId): Unit = {}
  override def read[U](key: U) = None
}

object AuthTest extends TestSuite with LogSupport {
  override def tests = Tests {
    'Auth - {
      val manager = newDesign
        .bind[Credentials].toInstance(Credentials("login", "password", ""))
        .bind[Credentials].toInstance(Core.defaultCredentials)
        .bind[Storage[String@@SessionId]].to[MocSessionStorage]
        .bind[Storage[String@@SessionId]].to[InMemoSessionStorage]
        .bind[AuthClient].to[AuthClientImpl]
        .bind[AuthClient].to[MocAuthClient]
        .newSession.build[AuthManager]
      for {
        id1 <- manager.sessionId
        id2 <- manager.sessionId
      } yield id1 ==> id2
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
