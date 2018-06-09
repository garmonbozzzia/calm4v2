package org.gbz.calm.model

import akka.http.scaladsl.model.Uri
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.attr
import org.gbz.calm.CalmEnums.{CourseTypes, CourseVenues}
import org.gbz.calm.Global._
import org.gbz.calm.{CalmUri, Parsers}
import org.json4s.jackson.JsonMethods.parse
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.gbz.Extensions._

object CourseListRequest extends CalmRequest[CourseList] {
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