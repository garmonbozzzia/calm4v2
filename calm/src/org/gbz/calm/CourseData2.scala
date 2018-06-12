package org.gbz.calm

import org.gbz.calm.Global._
import org.gbz.calm.model.AppListRequests.Applist2
import org.gbz.calm.model._

object CourseData2 {
  import org.gbz.Extensions._

  def merge(data1: AppList, data2: Applist2 ) = data1.apps
    .map(x => x -> data2(x.displayId))
    .map{ case (x,y) =>
      (s"${x.cId}:${x.aId}-${x.displayId}.app", x.ccToMap.mapValues(_.toString) ++ (y - "display_id"))
    }

  def update(course: CourseRecord) = for {
    courseData1 <- course.appListRequest1.http
    courseData2 <- course.appListRequest2.http
    kvs2 = merge(courseData1,courseData2)
  } yield CalmDb.update(kvs2)

  def export(course: CourseRecord) = for {
    courseData1 <- course.appListRequest1.http
    courseData2 <- course.appListRequest2.http
    kvs2 = merge(courseData1,courseData2)
  } yield CalmDb.export(kvs2)
}
