package calm.solid

import akka.http.scaladsl.Http
import calm.solid.MockModules._
import calm.solid.SandboxObjects._
import org.gbz.Global.{materializer, system, _}
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.Await
import scala.concurrent.duration._

object Stats extends LogSupport {
  import ammonite.ops._
  def print = {
    val path = pwd / 'calm / "4-apps"
//    %("git", "diff", "--stat", "HEAD")(path)
    %("git", "diff", "--stat", "HEAD")(path)

    val total = %%("git", "ls-files")(path).out.lines
      .map(Path(_, path))
      .map(read.lines)
      .map(_.size)
    s"Total: ${Console.RED}${total.sum}${Console.RESET} lines in ${Console.RED}${total.length}${Console.RESET} files".trace

    %%("git", "ls-files")(path).out.lines
      .map(%%("wc", "-l", _)(path))
      .flatMap(_.out.lines)
      .mkString("\n")
      .trace
    ("-"*80).trace
  }
}

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
    Stats.print

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
