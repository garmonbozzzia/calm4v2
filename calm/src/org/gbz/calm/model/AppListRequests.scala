package org.gbz.calm.model

import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.gbz.calm.Global._
import org.gbz.calm.{Calm, CalmUri}



object AppListRequests {
  type Applist2 = Map[String, Map[String,String]]
  def fromHtml(cId: String): CalmRequest[Applist2] = new CalmRequest[Applist2] {
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
    override def uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String) = (browser.parseString(data) >> elementList("tbody"))
      .last.>>(elementList("tr")).map(_.>>(elementList("td[id]")).map(x => x.attr("id") -> x.text).toMap)
      .map(_.filterKeys(fields.contains).map { case (k, v) => fieldsRename.getOrElse(k, k) -> v })
      .map(x => x("display_id").replace("*","") -> x).toMap
  }

  def fromJson(cId: String): CalmRequest[AppList] = new CalmRequest[AppList] {
    override def uri = CalmUri.courseUri(cId.toInt)
    override def parseEntity(data: String) = AppList(AppListParser.extractAppList(data))
    override def headers = Calm.xmlHeaders
  }
}