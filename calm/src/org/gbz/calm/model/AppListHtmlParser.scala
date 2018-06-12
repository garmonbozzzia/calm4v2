package org.gbz.calm.model

import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.gbz.calm.Global.browser
import org.gbz.calm.model.AppListRequests.AppList2
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList

object AppListHtmlParser {
  val fields = List(
    "app_rcvd",
    "display_id",
    "birth_date",
    "phone_home",
    "phone_mobile",
    "email",
    "incremented_enrolled_quota_at",
    "decremented_enrolled_quota_at"
  )

  val fieldsRename = Map(
    "app_rcvd" -> "receivedAt",
    "phone_home" -> "phoneHome",
    "phone_mobile" -> "phoneMobile",
    "incremented_enrolled_quota_at" -> "enrolled",
    "decremented_enrolled_quota_at" -> "dismissed"
  )

  def parse(data: String): AppList2 = (browser.parseString(data) >> elementList("tbody"))
    .last.>>(elementList("tr")).map(_.>>(elementList("td[id]")).map(x => x.attr("id") -> x.text).toMap)
    .map(_.filterKeys(fields.contains).map { case (k, v) => fieldsRename.getOrElse(k, k) -> v })
    .map(x => x("display_id").replace("*","") -> x).toMap
    .|>(AppList2(_))
}
