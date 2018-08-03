package calm_


import akka.http.scaladsl.model.headers.RawHeader
import calm.{AuthManager, Designs}
import org.gbz.Tag._
import utest._

import scala.concurrent.{Await, Future}



// 1

trait WebSource[T]
trait HtmlSource[T]
trait CourseListTag
object AllCourses extends CourseListTag


// 2


trait WebClient[A[_]] {
  def get[T: A](request: T): Future[String@@WebSource[T]]
}

// 3

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.headers.Cookie
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.util.ByteString

import scala.collection.immutable.{Seq => ISeq}


trait CalmUri[-A] {
  def uri(a:A): Uri
}

trait CalmHeaders[-A] {
  def headers(a:A): ISeq[HttpHeader]
}


trait CalmRequest[-T] extends CalmUri[T] with CalmHeaders[T]
object CalmRequest{
  def apply[A](implicit request: CalmRequest[A]): CalmRequest[A] = request
  def uri[A: CalmRequest](a:A): Uri = CalmRequest[A].uri(a)
  def headers[A: CalmRequest](a:A): ISeq[HttpHeader] = CalmRequest[A].headers(a)
  implicit class CalmRequestOps[T: CalmRequest](a: T){
    def uri: Uri = CalmRequest[T].uri(a)
    def headers: Seq[HttpHeader] = CalmRequest[T].headers(a)
  }

  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")
  val xmlHeaders: ISeq[HttpHeader] = scala.collection.immutable.Seq(accept,xml,referer)

}

import org.gbz.Global._
import wvlet.airframe._

trait Calm4WebClient extends WebClient[CalmRequest] {
  import CalmRequest._
  val auth = bind[AuthManager]
  override def get[T: CalmRequest](calmRequest: T): Future[String @@ WebSource[T]] = for {
    auth <- auth.sessionId.map(Cookie("_sso_session", _))
    request = Get(uri(calmRequest)).withHeaders(auth +: headers(calmRequest))
    response <- Http().singleRequest(request)
    json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
  } yield json.utf8String.@@[WebSource[T]]
//  override def get[T: CalmRequest[_]](request: T) = ???
}

object Sandbox extends TestSuite with Designs {
  override def tests = Tests{
    'WebClient - {
      val a = mainDesign.bind[WebClient[CalmRequest]].to[Calm4WebClient].newSession.build[WebClient[CalmRequest]]
      implicit val courseListRequest: CalmRequest[CourseListTag] = new CalmRequest[CourseListTag] {
        import calm.CalmUri._
        override def uri(a: CourseListTag) =  host.withPath("/en/courses").withQuery(columnParams(10) ++ Seq (
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

        override def headers(a: CourseListTag): ISeq[HttpHeader] = ISeq.empty[HttpHeader]
      }
      val b = a.get(AllCourses.taggedWith[HtmlSource[_]])
      b
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
