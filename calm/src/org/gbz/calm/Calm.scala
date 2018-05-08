package org.gbz.calm
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.headers.RawHeader
import akka.util.ByteString
import com.redis._
import org.gbz.calm.Global._

import scala.concurrent.Future

/* Created on 19.04.18 */

object Calm {
  val redisClient = new RedisClient("localhost", 6379, 1)
  val redisClientPool = new RedisClientPool("localhost", 6379, 8, 1)

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
    } yield calmRequest.parseEntity(json.utf8String)

  def html[Entity]: CalmRequest[Entity] => Future[String] = calmRequest =>
    for {
      auth <- Authentication.cookie
      request = Get(calmRequest.uri).withHeaders(auth)
      response <- Http().singleRequest(request)
      json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    } yield json.utf8String

  def redisCourseList: CourseList =
    CourseList(redisClientPool.withClient{ client => client.keys("*.course").get.flatten
    .map(client.hgetall1(_)).flatten
    .map(CourseRecord(_))
  })

  def redisAllApps = AppList(redisClientPool.withClient{ client =>
    client.keys("*:*.app").get.flatten
      .map(client.hgetall1(_)).flatten
      .map(ApplicantRecord(_))
  })

}
