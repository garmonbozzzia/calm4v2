package org.gbz.calm.model

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpHeader, Uri}
import akka.util.ByteString
import org.gbz.calm.Authentication
import org.gbz.calm.Global._
import org.gbz.ExtUtils._
import org.gbz.calm.model.AppListRequests.{AppList1, AppList2}

import scala.concurrent.Future

trait CalmRequest[Entity]{
  def uri: Uri
  def parseEntity(data: String): Entity
  def headers: scala.collection.immutable.Seq[HttpHeader] = Nil
  def http: Future[Entity] =
    for {
      auth <- Authentication.cookie
      request = Get(uri).withHeaders(auth +: headers)
      response <- Http().singleRequest(request)
      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield parseEntity(json.utf8String.trace)
}

object CalmRequest {
  def allCourses: CourseListRequest.type = CourseListRequest
  def courseAppsHtml(cId: CourseId): CalmRequest[AppList2] = AppListRequests.fromHtml(cId)
  def courseAppsJson(cId: CourseId): CalmRequest[AppList1] = AppListRequests.fromJson(cId)
  def courseApps(cId: CourseId) = ???
}