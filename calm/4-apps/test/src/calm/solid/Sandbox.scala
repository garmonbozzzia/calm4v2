package calm.solid

import akka.http.scaladsl.Http
import calm.solid.SandboxObjects._
import org.gbz.Global._
import utest._

import scala.concurrent.Await
import scala.concurrent.duration._

object Sandbox extends TestSuite with Designs {
  override def tests = Tests{
    'Implicits - {
      val a = mainDesign
//          .bind[Foo[Int]].toInstance(_ => "B")
        .newSession.build[Bar]
      a.bar
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
