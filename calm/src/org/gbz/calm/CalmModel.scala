package org.gbz.calm

import akka.http.scaladsl.model.{HttpHeader, Uri}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.gbz.Extensions._
import org.gbz.calm.Global._
import org.json4s._
import org.json4s.jackson.JsonMethods.parse

import scala.collection.immutable
import CalmEnums._

object CalmModel {
  trait CalmRequest[T]{
    def uri: Uri
    def parseEntity(data: String): T
    def headers: scala.collection.immutable.Seq[HttpHeader] = Nil
  }

  import CalmEnums.ApplicantStates._
  import CalmEnums.Genders._
  import CalmEnums.Roles._

  case class AppList(apps: Seq[ApplicantRecord]) {
    def filterT[V](extractor: ApplicantRecord => V)(st: V*) =
      AppList(apps.filter(x => st.contains(extractor(x))))

//    def n = AppList(apps.filter(_.role == NewStudent))
    def n = filterT(_.role)(NewStudent)
    def o = filterT(_.role)(OldStudent)
    def s = filterT(_.role)(Server)
    def m = filterT(_.gender)(Male)
    def f = filterT(_.gender)(Female)
    def complete: AppList = filterT(_.state)(Completed)
    def left: AppList = filterT(_.state)(Left)
    def cancelled: AppList = filterT(_.state)(Cancelled)

    def states: List[(ApplicantState, Int)] = ApplicantStates.values
      .map(x => x -> apps.count(_.state == x))
      .filter(_._2 > 0).toList.sortBy(-_._2)
    def ages: immutable.IndexedSeq[(Int, Int)] = (16 to 80).map(age => age -> apps.count(_.age == age))
  }

  case class CourseList(courses: Seq[CourseRecord]){
    import CourseTypes._
    def cType(cTypes: CourseType*) = CourseList(courses.filter(x => cTypes.contains(x.cType)))
    def c10d = cType(C10d)
    def c3d = cType(C3d)
    def c1d = cType(C1d)
    def sati = cType(Sati)
    def all = cType(C10d, C3d, C1d, Sati)
    def venue(vs: CourseVenue*) = CourseList(courses.filter(x => vs.contains(x.venue)))
    def dullabha = venue(CourseVenues.DD)
    def ekb = venue(CourseVenues.Ekb)
    def finished = CourseList(courses.filter(_.status == "Finished"))
    def scheduled = CourseList(courses.filter(_.status == "Scheduled"))
  }

  implicit object CourseListRequest extends CalmRequest[CourseList] {
    override def uri: Uri = CalmUri.coursesUri()
    override def parseEntity( data: String) =
      CourseList((parse(data) \ "data").extract[Seq[Seq[String]]].map(parseCourseRecord).flatten)
    import Parsers._

    private def parseCourseRecord: Seq[String] => Option[CourseRecord] = {
      case Seq(htmlStart, end, cType, venue, _, status, registrars, _, _, _, _) =>
        val html = browser.parseString(htmlStart)
        for {
          href <- html >?> attr("href")("a")
          id <- courseIdParser.fastParse(href)
        } yield CourseRecord( id.toString, (html >> text).replace("*",""), end, CourseTypes.withName(cType),
          CourseVenues.withName(venue), status)
      case x => x.trace; throw new Exception("error".trace)
    }
  }

  object CourseRecord {
    def apply(map: Map[String, String]): CourseRecord = CourseRecord(
      map("cId"), map("start"), map("end"), CourseTypes.withName(map("cType")),
      CourseVenues.withName(map("venue")), map("status")
    )
  }
  case class CourseRecord(cId: String, start: String, end: String,
                          cType: CourseType, venue: CourseVenue, status: String) {
    val dataRequest1 = CourseData2.dataRequest1(cId)
    val dataRequest2 = CourseData2.dataRequest2(cId)
  }

  object ApplicantRecord {
    def apply(map: Map[String,String]): ApplicantRecord = ApplicantRecord(
      map("cId"), map("aId").toInt, map("displayId"), map("givenName"), map("familyName"), map("age").toInt,
      Genders.withName(map("gender")), Roles.withName(map("role")), map("pregnant").toBoolean, map("nSat").toInt,
      map("nServe").toInt, ApplicantStates.withName(map("state"))
    )
  }

  case class ApplicantRecord(cId: String, aId: Int, displayId: String, givenName: String, familyName: String,
                             age: Int, gender: Gender, role: Role, pregnant: Boolean,
                             nSat: Int, nServe: Int, state: ApplicantState)

  case class ApplicantJsonRecord(id: Int, display_id: String, applicant_given_name: String, applicant_family_name: String,
                                 age: Option[Int], sitting: Boolean, old: Boolean, conversation_locale: String,
                                 language_native: String, ad_hoc: String, pregnant: Boolean, courses_sat: Option[Int],
                                 courses_served: Option[Int], room: String,
                                 hall_position: String, confirmation_state_name: String) {
    def app(cId: String, role: Role, gender: Gender) =
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

  def extractAppList(data: String) = {
    val json = parse(data)
    val cId = (json\"course_id").extract[Int]
    def f(jsonArray: JValue, role: Role, gender: Gender ) =
      jsonArray.extract[Seq[ApplicantJsonRecord]].sorted.map(_.app(cId.toString, role, gender))
    f(json \ "sitting" \ "male" \ "new", NewStudent, Male ) ++
      f(json \ "sitting" \ "male" \ "old", OldStudent, Male) ++
      f(json \ "sitting" \ "female" \ "new", NewStudent, Female) ++
      f(json \ "sitting" \ "female" \ "old", OldStudent, Female) ++
      f(json \ "serving" \ "male", Server, Male) ++
      f(json \ "serving" \ "female", Server, Female)
  }
}
