import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import com.redis.RedisClient
import org.apache.kafka.common.serialization.StringSerializer
import org.gbz.calm.CalmDb
import utest._

import scala.concurrent.Await

/* Created on 07.05.18 */
object RedisTests extends TestSuite {

  import org.gbz.calm.Global._
  import org.gbz.utils.log.Log._

  override def utestAfterAll(): Unit = {
    import scala.concurrent.duration._
    Await.result(system.terminate(), 1 minute)
  }

  val tests = Tests {
    trait Diff
    case class FieldChanges(key: String, fields: Map[String, (String, String)]) extends Diff
    case class NewKey(key: String) extends Diff
    case class DeleteKey(key: String) extends Diff
    case class NoChanges(key: String) extends Diff
    def update(key: String, data: Map[String, String]): RedisClient => Diff = rc => {
      if (rc.exists(key).trace) {
        val oldData = rc.hgetall1(key).get
        val changes = data.map {
          case (k, v) => k -> (oldData.getOrElse(k, "") -> v)
        }.filter { case (_, (v1, v2)) => v1 != v2 }
        if (changes.isEmpty) NoChanges(key) else {
          rc.hmset(key, data)
          FieldChanges(key,changes)
        }
      } else {
        rc.hmset(key, data)
        NewKey(key).trace
      }
    }
    val producer = KafkaProducer(KafkaProducer.Conf(new StringSerializer, new StringSerializer))
    val testTopic = "test"

    'RedisMap - {
      import org.json4s.jackson.Serialization.write
      //val testKey
      CalmDb.redisClient.del("k")
      val data = Seq( Map("a"->"aa", "b"->"bb"), Map("a"->"aa", "c"->"CCC", "b"->"BBB"), Map("a" -> "aa"))
      data.map(update("k",_)).map(CalmDb.redisClientPool.withClient).collect{
        case NewKey(k) => "NewKey" -> k
        case FieldChanges(key, fields) => key -> write(fields)
      }.map{case (k,v) => KafkaProducerRecord(testTopic, Some(k), v)}
        .foreach(producer.send)//foreach(Calm.redisClientPool.withClient(update("k", _)))
    }

    'Keys - {
      CalmDb.redisClient.keys("jddjjd")
    }

    'Redis - {
      CalmDb.redisClient.psetex("c4:testKey", 10000, "testValue2")
      CalmDb.redisClient.get("c4:testKey").trace
      CalmDb.redisClientPool.withClient(_ => {
        CalmDb.redisClient.psetex("c4:testKey1", 10000, "testValue1")
        CalmDb.redisClient.get("c4:testKey1").trace
      })
      CalmDb.redisClientPool.withClient(_ => {
        CalmDb.redisClient.psetex("c4:testKey2", 10000, "testValue2")
        CalmDb.redisClient.get("c4:testKey2").trace
      })
    }
  }
}