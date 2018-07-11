package org.gbz.calm.model

import akka.http.scaladsl.model.{HttpHeader, Uri}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.attr
import org.gbz.utils.log.Log._
import org.gbz.calm.CalmEnums.{CourseStatuses, CourseTypes, CourseVenues}
import org.gbz.calm.Global._
import org.gbz.calm.{Calm, CalmDb, CalmUri, Parsers}
import org.json4s.jackson.JsonMethods.parse

import scala.collection.immutable

trait RedisObject

trait RedisRequest[T <: RedisObject] {
  def key: String
  def fromMap: Map[String,String] => Option[T]
  def load: Option[T] = for {
    dataMap <- CalmDb.redisClient.hgetall1(key)
    result <- fromMap(dataMap)
  } yield result
}

object CourseListRequest extends CalmRequest[CourseList] {
  override def uri: Uri = CalmUri.coursesUri()

  override def headers: immutable.Seq[HttpHeader] = Calm.xmlHeaders
  override def parseEntity( data: String): CourseList =
    CourseList((parse(data) \ "data").extract[Seq[Seq[String]]].map(parseCourseRecord).flatten)
  import Parsers._

  private def parseCourseRecord: Seq[String] => Option[CourseRecord] = {
    case Seq(htmlStart, end, cType, venue, _, status, registrars, _, _, _, _) =>
      val html = browser.parseString(htmlStart)
      for {
        href <- html >?> attr("href")("a")
        id <- courseIdParser.fastParse(href)
      } yield CourseRecord( id, (html >> text).replace("*","") |> CourseDate.apply,
        CourseDate(end), CourseTypes.withName(cType),
        CourseVenues.withName(venue), CourseStatuses.withName(status))
    case x => x.trace; throw new Exception("error".trace)
  }
}
