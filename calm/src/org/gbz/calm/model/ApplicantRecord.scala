package org.gbz.calm.model
import org.gbz.calm.CalmEnums.{toString => _, _}

case class ApplicantRecord(cId: String, aId: Int, displayId: String, givenName: String, familyName: String,
                           age: Int, gender: Gender, role: Role, pregnant: Boolean,
                           nSat: Int, nServe: Int, state: ApplicantState)

object ApplicantRecord {
  def apply(map: Map[String,String]): ApplicantRecord = ApplicantRecord(
    map("cId"), map("aId").toInt, map("displayId"), map("givenName"), map("familyName"), map("age").toInt,
    Genders.withName(map("gender")), Roles.withName(map("role")), map("pregnant").toBoolean, map("nSat").toInt,
    map("nServe").toInt, ApplicantStates.withName(map("state"))
  )
}

