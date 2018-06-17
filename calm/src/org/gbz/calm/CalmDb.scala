package org.gbz.calm

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import com.redis.{RedisClient, RedisClientPool}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.json4s.jackson.Serialization.write
import org.gbz.ExtUtils._
import Global._
import org.gbz.calm.model.{AppList, ApplicantRecord, CourseList, CourseRecord}

object CalmDb {
  val redisClient = new RedisClient("localhost", 6379, 1)
  val redisClientPool = new RedisClientPool("localhost", 6379, 8, 1)
  trait Diff
  case class FieldChanges(key: String, fields: Map[String, (String, String)]) extends Diff
  case class NewKey(key: String) extends Diff
  case class DeleteKey(key: String) extends Diff
  case class NoChanges(key: String) extends Diff

  def update(keyPattern: String, data: Map[String, String]): RedisClient => Diff = rc => {
    rc.keys[String](keyPattern) match {
      case Some(Nil) =>
        rc.hmset(keyPattern, data)
        NewKey(keyPattern)
      case Some(Seq(Some(key))) =>
        val oldData = rc.hgetall1(key).get
        val changes = data.map {
          case (k, v) => k -> (oldData.getOrElse(k, "") -> v)
        }.filter { case (_, (v1, v2)) => v1 != v2 }
        if (changes.isEmpty) NoChanges(key) else FieldChanges(key,changes) <* rc.hmset(key, data)
    }
  }

  val updateTopic = "test"
  val producer = KafkaProducer(KafkaProducer.Conf(new StringSerializer, new StringSerializer))

  def keys: Any => Seq[(String, Map[String, String])] = {
    case x: (String, Map[String,String]) => Seq(x)
    case x: Seq[(String, Map[String,String])] => x
    case x: CourseList => x.courses.flatMap(keys)
    case x: AppList => x.apps.flatMap(keys)
//    case x: CourseData => x.allApps.flatMap(keys)
    case x: CourseRecord => Seq(s"${x.cId}.course" -> x.ccToMap.mapValues(_.toString))
    case x: ApplicantRecord => Seq(s"${x.cId}:${x.aId}-${x.displayId}.app" -> x.ccToMap.mapValues(_.toString))
  }

  def export(entity: Any): Seq[Boolean] = redisClientPool.withClient{ rc =>
    keys(entity).map { case (k, v) => rc.hmset(k, v) } }

  def update(entity: Any): Seq[ProducerRecord[String, String]] = redisClientPool.withClient{ rc =>
    keys(entity).map { case (k,v) => update(k,v)(rc)}.collect{
      case NewKey(k) => k -> s"NewKey $k"
      case x@FieldChanges(key, fields) => key -> s"ChangeFields $key ${write(fields)}"
    }.map{case (k,v) => KafkaProducerRecord(updateTopic, Some(k), v)}.map(_ <| producer.send)
  }
}
