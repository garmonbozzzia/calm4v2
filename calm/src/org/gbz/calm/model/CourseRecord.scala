package org.gbz.calm.model

import org.gbz.calm.CalmEnums.{CourseType, CourseTypes, CourseVenue, CourseVenues}
import org.gbz.calm.CourseData2

object CourseRecord {
  def apply(map: Map[String, String]): CourseRecord = CourseRecord(
    map("cId"), map("start"), map("end"), CourseTypes.withName(map("cType")),
    CourseVenues.withName(map("venue")), map("status")
  )
}
case class CourseRecord(cId: String, start: String, end: String,
                        cType: CourseType, venue: CourseVenue, status: String) {
  val appListRequest1 = AppListRequests.fromJson(cId)
  val appListRequest2 = AppListRequests.fromHtml(cId)
}
