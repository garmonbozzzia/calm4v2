package calm.solid

import akka.http.scaladsl.Http
import utest._
import scala.concurrent.Await
import concurrent.duration._
import org.gbz.utils.log.Log.LogSupport
import org.gbz.Global._

object WebClientDev extends TestSuite with LogSupport {
  override def tests = Tests {
    * - {}
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
