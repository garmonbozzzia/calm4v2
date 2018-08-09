package calm.solid

import akka.http.scaladsl.Http
import calm.solid.MockModules._
import calm.solid.SandboxObjects._
import org.gbz.Global.{materializer, system, _}
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.Await
import scala.concurrent.duration._

object Develop
    extends TestSuite
    with AppModule
    with MocAuthStorage
    with MocAuthClient
    with MocHtmlSource
    with MocJsonSource
    with MockRedisStorage
    with LogSupport {
  override def tests = Tests {
//    val aaaa: Option[Int @@ String] = Some(1)

    'Storage {
      write[Person](Alice)
      write[Person](Bob)
      read(Alice)
    }
  }

  override def utestAfterAll(): Unit = {
    super.utestAfterAll()
    Http().shutdownAllConnectionPools().andThen {
      case _ =>
        materializer.shutdown()
        system.terminate()
    }
    Await.ready(system.terminate(), 10 seconds)
  }
}
