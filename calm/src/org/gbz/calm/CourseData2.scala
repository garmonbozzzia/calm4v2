package org.gbz.calm

import org.gbz.calm.CalmModel.{ApplicantRecord, CalmRequest, CourseData, CourseRecord}
import org.gbz.calm.Global._
import org.json4s.jackson.JsonMethods.parse

object CourseData2 {
  import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
  import net.ruippeixotog.scalascraper.dsl.DSL._
  import org.gbz.Extensions._

  val fields = List(
    "app_rcvd",
    "display_id",
    "birth_date",
    "phone_home",
    "phone_mobile",
    "email",
    "incremented_enrolled_quota_at",
    "decremented_enrolled_quota_at"
  )

  val fieldsRename = Map(
    "app_rcvd" -> "receivedAt",
    "phone_home" -> "phoneHome",
    "phone_mobile" -> "phoneMobile",
    "incremented_enrolled_quota_at" -> "enrolled",
    "decremented_enrolled_quota_at" -> "dismissed"
  )

  type CourseData2 = Map[String, Map[String,String]]
  def dataRequest2(cId: String) = new CalmRequest[CourseData2] {
    override def uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String) = (browser.parseString(data) >> elementList("tbody"))
      .last.>>(elementList("tr")).map(_.>>(elementList("td[id]")).map(x => x.attr("id") -> x.text).toMap)
      .map(_.filterKeys(fields.contains).map { case (k, v) => fieldsRename.getOrElse(k, k) -> v })
      .map(x => x("display_id").replace("*","") -> x).toMap
  }

  def dataRequest1(cId: String) = new CalmRequest[CourseData] {
    override def uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String): CourseData = parse(data).extract[CourseData]
    override def headers = Calm.xmlHeaders
  }

  def merge(data1: CourseData, data2: CourseData2 ) = data1.allApps
    .map(x => x -> data2(x.displayId))
    .map{ case (x,y) =>
      (s"${x.cId}:${x.aId}-${x.displayId}.app", x.ccToMap.mapValues(_.toString) ++ (y - "display_id"))
    }

  def update(course: CourseRecord) = for {
    courseData1 <- Calm.http(course.dataRequest1)
    courseData2 <- Calm.http(course.dataRequest2)
    kvs2 = merge(courseData1,courseData2)
  } yield CalmDb.update(kvs2)

  def export(course: CourseRecord) = for {
    courseData1 <- Calm.http(course.dataRequest1)
    courseData2 <- Calm.http(course.dataRequest2)
    kvs2 = merge(courseData1,courseData2)
  } yield CalmDb.export(kvs2)
}
