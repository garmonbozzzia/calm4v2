package calm_


import akka.http.scaladsl.model.headers.RawHeader
import calm.{AuthManager, Designs}
import org.gbz.Tag._
import utest._

import scala.concurrent.{Await, Future}

// 1

trait WebSource[T]
trait HtmlSource[T]
trait JsonSource[T]
trait CourseListTag
object AllCourses extends CourseListTag
trait Result[-A] {
  type Result
}
object Result{
  type Aux[A,B] = Result[A]{type Result = B}
  type Auxx[A,B,F[_]] = F[A]{type Result = B}
}

// 2

trait WebClient[A[_] <: Result[_]] {
  def get[T,R](request: T)(implicit a: Result.Auxx[T,R,A]): Future[String@@R]
}

// 3

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.util.ByteString
import org.gbz.Global._
import wvlet.airframe._

import scala.collection.immutable.{Seq => ISeq}

trait CalmRequest[-T] extends Result[T]{
  def uri(a:T): Uri
  def headers: ISeq[HttpHeader]
}

object CalmRequest{
  type Aux[T,R] = CalmRequest[T]{type Result = R}
  def apply[A](implicit request: CalmRequest[A]): CalmRequest[A] = request
  def uri[A: CalmRequest](a:A): Uri = CalmRequest[A].uri(a)
  def headers[A: CalmRequest]: ISeq[HttpHeader] = CalmRequest[A].headers

  def html[T](f: T => Uri): CalmRequest[T] = new CalmRequest[T] {
    override type Result = HtmlSource[T]
    override def uri(a: T): Uri = uri(a)
    override def headers: ISeq[HttpHeader] = ISeq.empty[HttpHeader]
  }

  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")
  val xmlHeaders: ISeq[HttpHeader] = scala.collection.immutable.Seq(accept,xml,referer)

  def json[T](f: T => Uri): CalmRequest[T] = new CalmRequest[T] {
    override type Result = JsonSource[T]
    override def uri(a: T): Uri = uri(a)
    override def headers: ISeq[HttpHeader] = xmlHeaders
  }
}

trait Calm4WebClient extends WebClient[CalmRequest] {
  import CalmRequest._
  val auth = bind[AuthManager]
  override def get[T, R](calmRequest: T)(implicit a: CalmRequest.Aux[T, R]): Future[String @@ R] =
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

//      trait Bar[T]{
//        type Baz
//        def baz: Baz
//      }
//
//      object Baz{
//        type Aux[A,B] = Baz[A]{type Bazz = B}
//        def apply[A,B](foo: A => B): Aux[A,B] = new Baz[A] {
//          override type Bazz = B
//          override def baz(t: A): Bazz = foo(t)
//          def a[A:List:Option] = ???
//        }
//      }
//
//      trait Baz[T] {
//        type Bazz
//        def baz(t: T): Bazz
//      }
//
//      trait Foo[A[_]]{
//        def foo[T](implicit bar: Bar[T]): bar.Baz = bar.baz
//      }

      trait Foo[A,B]
      trait Bar[F[_,_]]{
        def bar[A,B](a:A)(implicit foo: Foo[A,B]): B
      }

//      implicit val barInt = new Bar[Int]{
//        override type Baz = String
//        override def baz = "Hello"
//      }



//      implicit val barInt: Baz.Aux[String,_] = Baz[String,Int](_ => 10)
//      new Foo[Bar]{}.foo
    }
    'WebClient - {
      val a = mainDesign.bind[WebClient[CalmRequest]].to[Calm4WebClient].newSession.build[WebClient[CalmRequest]]
      import calm.CalmUri._
      implicit val courseListRequest: CalmRequest[CourseListTag] = calm_.CalmRequest.html {
        _ => host.withPath("/en/courses").withQuery(columnParams(10) ++ Seq (
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
//      val b = a.get(AllCourses.taggedWith[HtmlSource[_]])
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
