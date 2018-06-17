package org.gbz.calm.model
import org.gbz.calm.CalmEnums.{toString => _, _}
import ammonite.ops.Extensions._

case class ApplicantRecord(cId: CourseId, aId: AppId, displayId: DisplayId, givenName: String, familyName: String,
                           age: Int, gender: Gender, role: Role, pregnant: Boolean,
                           nSat: Int, nServe: Int, state: ApplicantState)

object ApplicantRecord {
  def apply(data: Map[String,String]): ApplicantRecord = ApplicantRecord(
    cId        = data("cId").toInt,
    aId        = data("aId").toInt,
    displayId  = data("displayId"),
    givenName  = data("givenName"),
    familyName = data("familyName"),
    age        = data("age").toInt,
    gender     = data("gender") |> Genders.withName,
    role       = data("role") |> Roles.withName,
    pregnant   = data("pregnant").toBoolean,
    nSat       = data("nSat").toInt,
    nServe     = data("nServe").toInt,
    state      = data("state") |> ApplicantStates.withName
  )
}

