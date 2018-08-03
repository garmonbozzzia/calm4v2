package calm_

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.headers.{Cookie, RawHeader}
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.util.ByteString
import calm.{AuthManager, Designs}
import org.gbz.Global._
import org.gbz.Tag._
import utest._
import wvlet.airframe._

import scala.collection.immutable.{Seq => ISeq}
import scala.concurrent.{Await, Future}

// 1

trait HtmlSource[T]
trait JsonSource[T]
trait CourseListTag
object AllCourses extends CourseListTag

// 2

trait WebClient[A[_], B[_]] {
  def get[T:A,R:B](request: T): Future[String@@R]
  def html[T:A](request: T)(implicit b: B[HtmlSource[T]]): Future[String@@HtmlSource[T]] = get(request)
  def json[T:A](request: T)(implicit b: B[JsonSource[T]]): Future[String@@JsonSource[T]] = get(request)
}

// 3

trait CalmUri[-A]{
  def uri(a:A): Uri
}
trait CalmHeaders[+T]{
  def headers: ISeq[HttpHeader]
}

object CalmUri{
  def apply[A](implicit uri: CalmUri[A]): CalmUri[A] = uri
  def uri[A: CalmUri](a:A): Uri = CalmUri[A].uri(a)
}

object CalmHeaders{
  def apply[A](implicit request: CalmHeaders[A]): CalmHeaders[A] = request
  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")
  val xmlHeaders: ISeq[HttpHeader] = scala.collection.immutable.Seq(accept,xml,referer)
  implicit def html[T]: CalmHeaders[HtmlSource[T]] = new CalmHeaders[HtmlSource[T]] {
    override def headers: ISeq[HttpHeader] = ISeq.empty
  }
  implicit def json[T]: CalmHeaders[JsonSource[T]] = new CalmHeaders[JsonSource[T]] {
    override def headers: ISeq[HttpHeader] = xmlHeaders
  }
}

object Calm4WebClient {
  def uri[A: CalmUri](a:A): Uri = CalmUri[A].uri(a)
  def headers[A: CalmHeaders]: ISeq[HttpHeader] = CalmHeaders[A].headers

}

trait Calm4WebClient extends WebClient[CalmUri, CalmHeaders] {
  import Calm4WebClient._
  val auth = bind[AuthManager]
  override def get[T:CalmUri, R:CalmHeaders](calmRequest: T): Future[String @@ R] =
    for {
      auth <- auth.sessionId.map(Cookie("_sso_session", _))
      request = Get(uri(calmRequest)).withHeaders(auth +: headers)
      response <- Http().singleRequest(request)
      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield json.utf8String.@@[R]
}

// 4

object Sandbox extends TestSuite with Designs {
  override def tests = Tests{
    'SandBox - {

//      trait Foo[A]{
//        def foo(a:A):Int
//      }
//      trait Bar[F[_], G[_]]{
//        def bar[A:F,B:G](a:A) = implicitly[F[A]]
//      }
//      new Bar[Foo,Foo]{}
    }
    'WebClient - {
      type WebClientT = WebClient[CalmUri, CalmHeaders]
      val a: WebClientT = mainDesign.bind[WebClientT].to[Calm4WebClient].newSession.build[WebClientT]
      import calm.CalmUri._
      implicit val courseListUri: CalmUri[CourseListTag] = { _ =>
        host.withPath("/en/courses").withQuery(columnParams(10) ++ Seq (
          "order[0][column]" -> "0",
          "order[0][dir]" -> "asc",
          "start" -> "0",
          "length" -> "500",
          "search[value]" -> "",
          "search[regex]" -> "false",
          "user_custom_search[length]" -> "100",
          "user_custom_search[start]" -> "0",
          "user_custom_search[operator_start_date]" -> "gte_date",
          //  "user_custom_search[criterion_start_date]" -> startDate,
          "user_custom_search[operator_course_type_id]" -> "eq",
          "user_custom_search[filterOnMyCoursesOnly]" -> "false",
          "user_custom_search[defaultCurrentDate]" -> "true",
          "user_custom_search[context]" -> "all_courses"
        ))
      }
//      a.html(AllCourses)
//        a.get[CourseListTag, HtmlSource[CourseListTag]](AllCourses)
//      b
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
