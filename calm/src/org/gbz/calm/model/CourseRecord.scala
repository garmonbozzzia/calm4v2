package org.gbz.calm.model

import org.gbz.calm.CalmEnums._
import ammonite.ops.Extensions._

object CourseRecord {
  def apply(map: Map[String, String]): CourseRecord = CourseRecord(
    map("cId").toInt,
    map("start"),
    map("end"),
    map("cType") |> CourseTypes.withName,
    map("venue") |> CourseVenues.withName,
    map("status") |> CourseStatuses.withName,
  )
}

case class CourseRecord(cId: CourseId, start: String, end: String,
                        cType: CourseType, venue: CourseVenue, status: CourseStatus)
