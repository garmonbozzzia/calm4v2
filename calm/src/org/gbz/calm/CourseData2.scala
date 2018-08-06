package org.gbz.calm

import org.gbz.ExtUtils._
import org.gbz.calm.Global._
import org.gbz.calm.model._

object CourseData2 {

  def update(course: CourseRecord) =
    AppListRequests.merged(course.cId).map(x => CalmDb.update(x.ccToMap))

  def export(course: CourseRecord) =
    AppListRequests.merged(course.cId).map(x => CalmDb.export(x.ccToMap))
}
