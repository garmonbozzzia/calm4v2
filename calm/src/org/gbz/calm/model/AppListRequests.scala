package org.gbz.calm.model

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.RawHeader
import org.apache.kafka.clients.producer.ProducerRecord
import org.gbz.calm.Global._
import org.gbz.calm.{Calm, CalmDb, CalmUri}

import scala.collection.immutable
import scala.concurrent.Future



object AppListRequests {
  type CourseId = String
  type AppList1 = Seq[ApplicantRecord]
  type AppList2 = Map[DisplayId, ApplicantHtmlRecord]

  def fromHtml(cId: String) = new CalmRequest[AppList2] {

    override def uri: Uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String) = AppListHtmlParser.parse(data)
  }

  def fromJson(cId: String): CalmRequest[AppList1] = new CalmRequest[AppList1] {
    override def uri: Uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String): Seq[ApplicantRecord] = AppListJsonParser.extractAppList(data)
    override def headers: immutable.Seq[RawHeader] = Calm.xmlHeaders
  }

  import org.gbz.Extensions._
  import ammonite.ops.Extensions._
  type DisplayId = String

  def merge(data1: AppList1, data2: AppList2 ): AppList =
    data1.map(x => MergedApplicantRecord(x, data2(x.displayId)))
      .|>(AppList)

  //todo replace
  def update(cId: CourseId): Future[Seq[ProducerRecord[String, String]]] = for {
    courseData1 <- fromJson(cId).http
    courseData2 <- fromHtml(cId).http
    kvs2 = merge(courseData1,courseData2)
  } yield CalmDb.update(kvs2)

  def merged(cId : CourseId) = for {
    courseData1 <- fromJson(cId).http
    courseData2 <- fromHtml(cId).http
  } yield merge(courseData1,courseData2)
}