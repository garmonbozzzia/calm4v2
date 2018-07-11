package org.gbz.calm.model

import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.gbz.calm.Global.browser
import org.gbz.calm.model.AppListRequests.AppList2
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.gbz.calm.Global

import scala.collection.immutable
import scala.util.Try

case class ApplicantHtmlRecord (receivedAt: String, displayId: String, birthDate: String, email: String,
                                phoneHome: String, phoneMobile: String, enrolledAt: String, dismissedAt: String)

import org.gbz.utils.log.Log._

object ApplicantHtmlRecord {
  def apply(data: Map[String, String]): Option[ApplicantHtmlRecord] = Try{ new ApplicantHtmlRecord(
    receivedAt = data("app_rcvd"),
    displayId = data("display_id").replace("*",""),
    birthDate = data("birth_date"),
    phoneHome = data("phone_home"),
    phoneMobile = data("phone_mobile"),
    email = data("email"),
    enrolledAt = data("incremented_enrolled_quota_at"),
    dismissedAt = data("decremented_enrolled_quota_at")
  )}.fold(e => None.trace(e), Some(_))
}

object AppListHtmlParser {

  def parse(data: String): Map[String, ApplicantHtmlRecord] = (browser.parseString(data) >> elementList("tbody"))
    .last.>>(elementList("tr")).map(_.>>(elementList("td[id]")).map(x => x.attr("id") -> x.text).toMap)
    .map(x => ApplicantHtmlRecord(x)).flatten
    .map(x => x.displayId -> x).toMap
}
