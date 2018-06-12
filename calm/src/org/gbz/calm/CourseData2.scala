package org.gbz.calm

import org.gbz.calm.Global._
import org.gbz.calm.model.AppListRequests.AppList2
import org.gbz.calm.model._

object CourseData2 {
  import org.gbz.Extensions._

  type DisplayId = String
  case class AppList2Redis(apps: Map[DisplayId,Map[String,String]])

  def merge(data1: AppList, data2: AppList2 ) = data1.apps
    .map(x => x -> data2.apps(x.displayId))
    .map{ case (x,y) =>
      (s"${x.cId}:${x.aId}-${x.displayId}.app", x.ccToMap.mapValues(_.toString) ++ (y - "display_id"))
    }

  def update(course: CourseRecord) = for {
    courseData1 <- AppListRequests.fromJson(course.cId).http
    courseData2 <- AppListRequests.fromHtml(course.cId).http
    kvs2 = merge(courseData1,courseData2)
  } yield CalmDb.update(kvs2)

  def export(course: CourseRecord) = for {
    courseData1 <- AppListRequests.fromJson(course.cId).http
    courseData2 <- AppListRequests.fromHtml(course.cId).http
    kvs2 = merge(courseData1,courseData2)
  } yield CalmDb.export(kvs2)
}
