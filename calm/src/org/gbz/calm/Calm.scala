package org.gbz.calm
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.headers.RawHeader
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.util.ByteString
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import org.gbz.calm.Global._
import org.gbz.calm.model._

import scala.concurrent.Future

/* Created on 19.04.18 */

object Calm {
  val accept = RawHeader("Accept", "application/json, text/javascript, */*; q=0.01")
  val xml = RawHeader("X-Requested-With", "XmlHttpRequest")
  val referer = RawHeader("Referer", "")

  val xmlHeaders = scala.collection.immutable.Seq(accept,xml,referer)

  import CalmDb._

  val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers("localhost:9092")
    .withGroupId("group1")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  def consumer = Consumer.committableSource(consumerSettings, Subscriptions.topics("wstest"))


  //def http2 =

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