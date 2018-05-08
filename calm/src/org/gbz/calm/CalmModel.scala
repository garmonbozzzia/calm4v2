package org.gbz.calm

import akka.http.scaladsl.model.Uri
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.gbz.Extensions._
import org.gbz.calm.Global.{browser, calmStates, _}
import org.json4s._
import org.json4s.jackson.JsonMethods.parse

import scala.collection.immutable

object CalmModel {
  trait CalmRequest[T]{
    def uri: Uri
    def parseEntity(data: String): T
  }

  case class AppList(apps: Seq[ApplicantRecord]) {
    def n = AppList(apps.filter(_.role == "N"))
    def o = AppList(apps.filter(_.role == "O"))
    def s = AppList(apps.filter(_.role == "S"))
    def m = AppList(apps.filter(_.gender == "M"))
    def f = AppList(apps.filter(_.gender == "F"))
    def filter(f: ApplicantRecord => Boolean) = AppList(apps.filter(f))
    def complete: AppList = st("Completed")
    def left = st("Left")
    def cancelled = st("Cancelled")
    def st(st: String*) = AppList(apps.filter(x => st.contains(x.state)))
    def stN(st: String*) = AppList(apps.filterNot(x => st.contains(x.state)))

    def states: Seq[(String, Int)] = calmStates.map(x => x -> apps.count(_.state == x)).filter(_._2 > 0).sortBy(-_._2)
    def ages: immutable.IndexedSeq[(Int, Int)] = (16 to 80).map(age => age -> apps.count(_.age == age))
  }

//  object CourseList {
//    def keyPattern = "*.course"
//    def all = Calm.redisClientPool.withClient(rc => rc.keys(keyPattern).get.flatten.map())
//  }

  case class CourseList(courses: Seq[CourseRecord]){
    def c10d = CourseList(courses.filter(_.cType == "10-Day"))
    def dullabha = CourseList(courses.filter(_.venue == "Dhamma Dullabha"))
    def finished = CourseList(courses.filter(_.status == "Finished"))
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
        } yield CourseRecord( id.toString, (html >> text).replace("*",""), end, cType, venue, status)
      case x => x.trace; throw new Exception("error".trace)
    }
  }

  object CourseRecord {
    def apply(map: Map[String, String]): CourseRecord = CourseRecord(
      map("cId"), map("start"), map("end"), map("cType"), map("venue"), map("status")
    )
  }
  case class CourseRecord(cId: String, start: String, end: String, cType: String, venue: String, status: String)
    extends CalmRequest[CourseData]{
    override def uri: Uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String): CourseData = parse(data).extract[CourseData]
  }

  object ApplicantRecord {
    def apply: ApplicantJsonRecord => ApplicantRecord = {
      case ApplicantJsonRecord(id, display_id, applicant_given_name, applicant_family_name,
      age, sitting, old, _, _, ad_hoc, pregnant, courses_sat, courses_served, _, _, state) => ???
    }

    def apply(map: Map[String,String]): ApplicantRecord = ApplicantRecord(
      map("cId"), map("aId").toInt, map("displayId"), map("givenName"), map("familyName"), map("age").toInt,
      map("gender"), map("role"), map("pregnant").toBoolean, map("nSat").toInt, map("nServe").toInt, map("state")
    )
  }

  case class ApplicantRecord(cId: String, aId: Int, displayId: String, givenName: String, familyName: String,
                             age: Int, gender: String, role: String, pregnant: Boolean,
                             nSat: Int, nServe: Int, state: String)

  case class ApplicantJsonRecord(id: Int, display_id: String, applicant_given_name: String, applicant_family_name: String,
                                 age: Option[Int], sitting: Boolean, old: Boolean, conversation_locale: String,
                                 language_native: String, ad_hoc: String, pregnant: Boolean, courses_sat: Option[Int],
                                 courses_served: Option[Int], room: String,
                                 hall_position: String, confirmation_state_name: String) {
    def app(cId: String, role: String, gender: String) = ApplicantRecord(cId, id,display_id,applicant_given_name, applicant_family_name,
      age.getOrElse(-1), gender, role, pregnant, courses_sat.getOrElse(0), courses_served.getOrElse(0), confirmation_state_name)
  }

  case class OldNew(old: Seq[ApplicantJsonRecord], `new`: Seq[ApplicantJsonRecord])

  case class MaleFemaleSittings(male: OldNew, female: OldNew)

  case class MaleFemaleServing(male: Seq[ApplicantJsonRecord], female: Seq[ApplicantJsonRecord])

  object ApplicantRecordOrd extends Ordering[ApplicantJsonRecord] {
    override def compare(x: ApplicantJsonRecord, y: ApplicantJsonRecord): Int = {
      if(x.confirmation_state_name == y.confirmation_state_name)
        x.applicant_family_name.compare(y.applicant_family_name)
      else calmStates.indexOf(x.confirmation_state_name) - calmStates.indexOf(y.confirmation_state_name)
    }
  }
  case class CourseData(course_id: Int, venue_name: String, start_date: String, end_date: String,
                        user_can_assign_hall_position: Boolean, sitting: MaleFemaleSittings,
                        serving: MaleFemaleServing) {
    implicit val ord: Ordering[ApplicantJsonRecord] = ApplicantRecordOrd

    def app: (String, String) => (ApplicantJsonRecord => ApplicantRecord) =
      (a,b) => x => x.app(course_id.toString, a, b)

    lazy val allApps: Seq[ApplicantRecord] =
      sitting.male.`new`.sorted.map(app("N", "M")) ++
      sitting.male.old.sorted.map(app("O", "M")) ++
      sitting.female.`new`.sorted.map(app("N", "F")) ++
      sitting.female.old.sorted.map(app("O", "F")) ++
      serving.male.sorted.map(app("S", "M")) ++
      serving.female.sorted.map(app("S", "F"))

    lazy val all = sitting.male.`new`.sorted ++
      sitting.male.old.sorted ++
      sitting.female.`new`.sorted ++
      sitting.female.old.sorted ++
      serving.male.sorted ++
      serving.female.sorted
  }

}