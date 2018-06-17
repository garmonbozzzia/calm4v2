package org.gbz.calm

import org.gbz.calm.Global._
import org.gbz.calm.model.AppListRequests.AppList2
import org.gbz.calm.model._

object CourseData2 {
  import org.gbz.ExtUtils._

  def update(course: CourseRecord) =
    AppListRequests.merged(course.cId).map(x => CalmDb.update(x.ccToMap))

  def export(course: CourseRecord) =
    AppListRequests.merged(course.cId).map(x => CalmDb.export(x.ccToMap))
}
