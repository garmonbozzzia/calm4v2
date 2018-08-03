package calm.solid

import akka.http.scaladsl.Http
import ammonite.ops.{Path, read => readFile, write => writeFile}
import org.gbz.Global._
import org.gbz.Tag._
import utest._

import scala.concurrent.{Await, Future}
import scala.util.Try

trait Key[T]{
  def key(t:T): Path
}
object Key {
  def apply[T](implicit key: Key[T]): Key[T] = key
}
trait Validator[T]

class CachedWebClient extends WebClient[Key,Validator] {
  override def get[T: Key, R: Validator](request: T): Future[String@@R] = {
    val path = Key[T].key(request)
    Try(readFile(path)).map(_.@@[R]).fold(Future.failed, Future.successful)
  }
}

trait WebClientA {
  type A[_]
  type B[_]
  def get[T:A,R:B](request: T): Future[String@@R]
  def html[T:A](request: T)(implicit b: B[HtmlSource[T]]): Future[String@@HtmlSource[T]] =
    get[T,HtmlSource[T]](request)
  def json[T:A](request: T)(implicit b: B[JsonSource[T]]): Future[String@@JsonSource[T]] =
    get[T,JsonSource[T]](request)
}



object Sandbox extends TestSuite with Designs {
  override def tests = Tests{
    'SandBox - {

    }

    'WebClient - {
      import CalmUri._
      val a: WebClient[CalmUri, CalmHeaders] = mainDesign
//          .bind[WebClient]
        .newSession.build[WebClient[CalmUri, CalmHeaders]]
//      val b: Future[String @@ HtmlSource[AllCourses.type]] = a.html(AllCourses)
//      a.get[CourseListTag, HtmlSource[CourseListTag]](AllCourses)
//      b

      val c = new WebClientA {
        override type A[_] = CalmUri[_]
        override type B[_] = CalmHeaders[_]

        override def get[T: A, R: B](request: T) = ???
      }
//      val d: HtmlSource[CourseListTag] = c.get[CalmUri, CalmHeaders, CourseListTag, HtmlSource[CourseListTag]](AllCourses)
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
