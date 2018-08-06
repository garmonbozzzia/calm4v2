package org.gbz.calm.model

import org.gbz.calm.CalmEnums.Genders.{Female, Male}
import org.gbz.calm.CalmEnums.{ApplicantStates, Gender, Role}
import org.gbz.calm.CalmEnums.Roles.{NewStudent, OldStudent, Server}
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.parse
import org.gbz.calm.Global._
import org.json4s._
import org.json4s.jackson.JsonMethods.parse

object AppListJsonParser {
  case class ApplicantJsonRecord(id: Int, display_id: String, applicant_given_name: String, applicant_family_name: String,
                                 age: Option[Int], sitting: Boolean, old: Boolean, conversation_locale: String,
                                 language_native: String, ad_hoc: String, pregnant: Boolean, courses_sat: Option[Int],
                                 courses_served: Option[Int], room: String,
                                 hall_position: String, confirmation_state_name: String) {
    def app(cId: CourseId, role: Role, gender: Gender) =
      ApplicantRecord(cId, id,display_id,applicant_given_name, applicant_family_name,
        age.getOrElse(-1), gender, role, pregnant, courses_sat.getOrElse(0), courses_served.getOrElse(0),
        ApplicantStates.withName(confirmation_state_name))
  }

  object ApplicantRecordOrd extends Ordering[ApplicantJsonRecord] {
    override def compare(x: ApplicantJsonRecord, y: ApplicantJsonRecord): Int = {
      if(x.confirmation_state_name == y.confirmation_state_name)
        x.applicant_family_name.compare(y.applicant_family_name)
      else ApplicantStates.values.toList.map(_.toString).indexOf(x.confirmation_state_name) -
        ApplicantStates.values.toList.map(_.toString).indexOf(y.confirmation_state_name)
    }
  }
  implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd

  case class CourseDataOnly(course_id: Int, venue_name: String, start_date: String, end_date: String)

  def extractAppList(data: String): Seq[ApplicantRecord] = {
    val json = parse(data)
    val cId = (json\"course_id").extract[Int]
    def f(jsonArray: JValue, gender: Gender, role: Role ) =
      jsonArray.extract[Seq[ApplicantJsonRecord]].sorted.map(_.app(cId, role, gender))
    f(json \ "sitting" \ "male" \ "new", Male, NewStudent) ++
      f(json \ "sitting" \ "male" \ "old", Male, OldStudent) ++
      f(json \ "sitting" \ "female" \ "new", Female, NewStudent) ++
      f(json \ "sitting" \ "female" \ "old", Female, OldStudent) ++
      f(json \ "serving" \ "male", Male, Server) ++
      f(json \ "serving" \ "female", Female, Server)
  }
}
