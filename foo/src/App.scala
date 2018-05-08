import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import com.typesafe.config.Config
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