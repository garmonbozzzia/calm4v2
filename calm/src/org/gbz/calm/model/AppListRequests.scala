package org.gbz.calm.model

import akka.http.scaladsl.model.Uri

import org.gbz.calm.Global._
import org.gbz.calm.{Calm, CalmDb, CalmUri}

import scala.concurrent.Future
import org.gbz.Extensions._



object AppListRequests {
  type CourseId = String
  case class AppList2(apps: Map[String, Map[String,String]])
  def fromHtml(cId: String): CalmRequest[AppList2] = new CalmRequest[AppList2] {

    override def uri: Uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String): AppList2 = AppListHtmlParser.parse(data)
  }

  def fromJson(cId: String): CalmRequest[AppList] = new CalmRequest[AppList] {
    override def uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String) = AppList(AppListJsonParser.extractAppList(data))
    override def headers = Calm.xmlHeaders
  }

  import org.gbz.Extensions._
  type DisplayId = String
  case class MergedAppList(apps: Map[DisplayId,Map[String,String]])


  def merge(data1: AppList, data2: AppList2 ) = {
    val z = data1.apps
      .map(x => x -> data2.apps(x.displayId))
      .map { case (x, y) =>
        (s"${x.cId}:${x.aId}-${x.displayId}.app", x.ccToMap.mapValues(_.toString) ++ (y - "display_id"))
      }.toMap
    MergedAppList(z)
  }

  //todo replace
  def update(cId: CourseId) = for {
    courseData1 <- fromJson(cId).http
    courseData2 <- fromHtml(cId).http
    kvs2 = merge(courseData1,courseData2)
  } yield CalmDb.update(kvs2)

  def merged(cId : CourseId): Future[MergedAppList] = for {
    courseData1 <- fromJson(cId).http
    courseData2 <- fromHtml(cId).http
  } yield merge(courseData1,courseData2)
}