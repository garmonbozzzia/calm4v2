/* Created on 08.05.18 */

import akka.kafka._
import akka.kafka.scaladsl._
import akka.stream.scaladsl._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization._
import utest._

object KafkaTest extends TestSuite {
  import org.gbz.utils.log.Log._
  import org.gbz.calm.Global._
  val tests = Tests {
    'Hello - {
      val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("localhost:9092")
      val done = Source(1 to 100)
        .map(_.toString)
        .map { elem =>
          new ProducerRecord[Array[Byte], String]("topic1", elem)
        }
        .runWith(Producer.plainSink(producerSettings))
      val kafkaProducer = producerSettings.createKafkaProducer()
      kafkaProducer.metrics()

      val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

      Consumer.committableSource(consumerSettings, Subscriptions.topics("topic1"))
        .runForeach(_.record.value.trace)
    }
    'Client - {
      import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
      import org.apache.kafka.common.serialization.StringSerializer

      val producer = KafkaProducer(KafkaProducer.Conf(new StringSerializer, new StringSerializer))
      val topic = "topic"

      def close() = producer.close()

      producer.send(KafkaProducerRecord(topic, "A"))
//        val consumer = KafkaConsumer(KafkaConsumer.Conf(new StringDeserializer(),
      //          new StringDeserializer(), groupId = "")
      //        )
      //        consumer.subscribe(Seq(topic))

      val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group1")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

      val a = Consumer.committableSource(consumerSettings, Subscriptions.topics("topic"))
        .runForeach(_.record.value.trace)

      producer.send(KafkaProducerRecord(topic, "B"))
      a
    }
  }
}
