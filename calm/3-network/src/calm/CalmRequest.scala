package calm

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.headers.{Cookie, RawHeader}
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.util.ByteString
import org.gbz.Global._
import org.gbz.Tag._
import wvlet.airframe._

import scala.collection.immutable
import scala.concurrent.Future


trait CalmRequest[Entity]
object CourseList extends CalmRequest[CourseList]
case class CourseRecord()
case class CourseList(courses: Seq[CourseRecord])

// CourseList.http[JsonCourseList]
// CourseList.storage[JsonCourseList]
//AllCourses.http[WebSource@@CourseList].map(extract[CourseList])
//AllCourses.fromStorage[CourseList]
//AllCourses.get[CourseList]
//


object WebClient{
  trait WebSource
  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")
  val xmlHeaders = scala.collection.immutable.Seq(accept,xml,referer)

  trait HeaderHelper[T] {
    def headers(request: CalmRequest[T]): scala.collection.immutable.Seq[HttpHeader]
  }
  trait UriHelper[T] {
    def uri(request: CalmRequest[T]): Uri
  }
  trait Helper[T] extends UriHelper[T] with HeaderHelper[T]
  implicit def defaultHeaderHelper[T]: HeaderHelper[T] = _ => scala.collection.immutable.Seq.empty[HttpHeader]
  implicit def f[T](implicit a: UriHelper[T], b: HeaderHelper[T]): Helper[T] = new Helper[T] {
    override def uri(request: CalmRequest[T]): Uri = a.uri(request)
    override def headers(request: CalmRequest[T]): immutable.Seq[HttpHeader] = b.headers(request)
  }
}
import WebClient._

trait WebClient{
  val auth = bind[AuthManager]
  def get[T: Helper](calmRequest: CalmRequest[T]): Future[String@@WebSource] =
    for {
      auth <- auth.sessionId.map(Cookie("_sso_session", _))
      helper = implicitly[Helper[T]]
      request = Get(helper.uri(calmRequest))
        .withHeaders(auth +: helper.headers(calmRequest))
      response <- Http().singleRequest(request)
      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield json.utf8String.@@[WebSource]
}

object WebClientImplicits {
  import WebClient._
  implicit val courseListHelper: UriHelper[CourseList] = {
    case CourseList => CalmUri.coursesUri()
  }
  implicit val courseListHeaders: HeaderHelper[CourseList] = {
    case CourseList => xmlHeaders
  }

}

//
//object Sandbox {
//  import org.gbz.ExtUtils._
//
//  trait AppId
//  trait AppHtml
//  trait Helper[T,S]
//  def http[S,T](request: S)(implicit helper: Helper[S,T]): Future[WebSource@@T] = ???
//  implicit val helper = new Helper[Int@@AppId, AppHtml]{
//
//  }
//  1.@@[AppId] rapl rapl http
//  trait WebSource{
//    def extract[T](t: Option[T])
//  }
//  trait RequestHelper[T]
//}
