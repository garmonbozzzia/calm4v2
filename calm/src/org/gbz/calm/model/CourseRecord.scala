package org.gbz.calm.model

import java.util.Date

import org.gbz.calm.CalmEnums._
import ammonite.ops.Extensions._
import org.gbz.calm.Global._

object CourseRecord {
  def apply(map: Map[String, String]): CourseRecord = CourseRecord(
    map("cId").toInt,
    map("start") |> CourseDate.apply,
    map("end") |> CourseDate.apply,
    map("cType").toInt |> CourseTypes.apply,
    map("venue").toInt |> CourseVenues.apply,
    map("status").toInt |> CourseStatuses.apply,
  )
}

case class CourseRecord(cId: CourseId,
                        start: CourseDate,
                        end: CourseDate,
                        cType: CourseType,
                        venue: CourseVenue,
                        status: CourseStatus)
