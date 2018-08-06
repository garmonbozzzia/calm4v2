package calm.solid

import akka.http.scaladsl.Http
import org.gbz.Global.{materializer, system, _}
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.higherKinds

object AuthDev extends
  TestSuite with
  LogSupport with
  Designs with
  AuthMocModule {

  override implicit val authClient: AuthDev.AuthClient = mocAuthClient

  override def tests = Tests{
    'Storage{
      implicitly[AuthManager].sessionId
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