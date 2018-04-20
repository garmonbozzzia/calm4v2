package org.gbz.calm
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.RawHeader
import akka.util.ByteString
import com.redis._
import Global._
import org.gbz.Extensions._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._

import scala.concurrent.Future
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.native.Serialization.{read, write}

/* Created on 19.04.18 */

object SessionId

object CalmModel {
  //trait CalmEntity
  trait CalmRequest[T]{
    def uri: Uri
    def parseEntity(data: String): T
  }
  trait CalmParser[T] { def parse(s: String): T }

  case class CourseRecord(cId: String, start: String, end: String, cType: String, venue: String, status: String)
  //  case class CourseList(courses: Seq[CourseRecord])
  //  case class CourseList(courses: Seq[Seq[String]])
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
}

import ammonite.ops._
object Calm {
  val redisClient = new RedisClient("localhost", 6379)
  val redisClientPool = new RedisClientPool("localhost", 6379)

  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")

  import CalmModel._
  def http[Entity]: CalmRequest[Entity] => Future[Entity] = calmRequest =>
    for {
      auth <- Authentication.cookie
      request = Get(calmRequest.uri).withHeaders(auth, xml, accept, referer)
      response <- Http().singleRequest(request)
      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
      data = (parse(json.utf8String) \ "data").extract[Seq[Seq[String]]]
    } yield calmRequest.parseEntity(json.utf8String)

  def export2redis: Any => Any = {
    case CourseList(courses) => courses.foreach(export2redis)
    case x@CourseRecord(cId, start, end, cType, venue, status) =>
      redisClient.hmset(s"c4.cs.$cId.", x.ccToMap)
  }

  def courseRecordFromMap(map: Map[String, String]): CourseRecord = CourseRecord(
    map("cId"), map("start"), map("end"), map("cType"), map("venue"), map("status")
  )
  def loadCourseList: CourseList = CourseList(redisClientPool.withClient{ client => client.keys("c4.cs.*.").get.flatten
    .map(client.hgetall1(_)).flatten
    .map(courseRecordFromMap(_))
    //.map(fromMap[CourseRecord](_))
  })
}
