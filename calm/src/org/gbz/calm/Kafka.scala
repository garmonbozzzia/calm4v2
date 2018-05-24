import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import cakesolutions.kafka.{KafkaConsumer, KafkaProducer, KafkaProducerRecord}
import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.internals.PartitionAssignor.Subscription
import org.apache.kafka.common.serialization.StringSerializer

class SampleSubmitter(config: Config) {

  private val producer = KafkaProducer(
    KafkaProducer.Conf(
      config,
      keySerializer = new StringSerializer,
      valueSerializer = new StringSerializer)
  )
  private val topic = config.getString("topic")
  def close() = producer.close()
}

object Main extends App {
  println("H1")
}