package org.gbz.calm.model

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.util.ByteString
import org.gbz.calm.AuthenticationBind
import org.gbz.calm.Global._
import org.gbz.calm.model.AppListRequests.{AppList1, AppList2}

import scala.concurrent.Future

trait CalmRequest[Entity] extends AuthenticationBind{
  def uri: Uri
  def headers: scala.collection.immutable.Seq[HttpHeader] = Nil
  def http(implicit parser: Parser[Entity]): Future[Entity] = ???
//    for {
//      auth <- authentication.cookie
//      request = Get(uri).withHeaders(auth +: headers)
//      response <- Http().singleRequest(request)
//      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
//    } yield parseEntity(json.utf8String)
}

trait Parser[T] {
  def parse: String => T
}
object Parser {

  def apply[T](implicit parser: Parser[T]): Parser[T] = parser
  def pure[T](f: String => T): Parser[T] = new Parser[T] {
    override def parse = f
  }
  implicit val asJsonParser: Parser[Seq[ApplicantRecord]] = pure(AppListJsonParser.extractAppList)
  implicit val asHtmlParser: Parser[Map[String, ApplicantHtmlRecord]] = pure(AppListHtmlParser.parse)
  implicit val csJsonParser: Parser[CourseList] = pure(CourseListRequest.parseEntity)

}

object CalmRequest{

  def allCourses: CalmRequest[CourseList] = CourseListRequest
  def courseAppsHtml(cId: CourseId): CalmRequest[AppList2] = AppListRequests.fromHtml(cId)
  def courseAppsJson(cId: CourseId): CalmRequest[AppList1] = AppListRequests.fromJson(cId)
}
