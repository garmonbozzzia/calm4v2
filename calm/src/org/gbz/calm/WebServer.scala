import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.{OverflowStrategy, ThrottleMode}
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.gbz.calm.CalmDb
import org.gbz.calm.Global._
import org.json4s.jackson.Serialization.write

import scala.io.StdIn


trait CalmEvent {
  def messageId: Int
}
case class NewFormEvent(messageId: Int) extends CalmEvent
case class FieldChanged(messageId: Int) extends CalmEvent
case class Check(messageId: Int) extends CalmEvent
case class UnCheck(messageId: Int) extends CalmEvent
case class UserData(unchecked: List[CalmEvent], checked: List[CalmEvent])
object UserDataProcessor{
  val cId = 4053
  import scala.concurrent.duration._
  def update = ???
  def calmEventSource = Source.fromIterator(() => CalmDb.redisClientPool.withClient { rc =>
    rc.keys(s"$cId:*.app").get.flatten.map(x => rc.hgetall1(x).get).map(x => write(x))
  }.take(10).iterator)
  .throttle(1, 1 second, 1, ThrottleMode.Shaping)
  calmEventSource.expand(Iterator.continually(_))
  val controlSource = Source.queue[CalmEvent](100, OverflowStrategy.fail)
}

object WebServer {
  def main(args: Array[String]) {
    import scala.concurrent.duration._
    import org.gbz.ExtUtils._

    val cId = 4053
    def greeter: Flow[Message, Message, Any] = Flow.fromSinkAndSource(Sink.foreach{
        case tm: TextMessage => tm.textStream.runWith(Sink.ignore)
        case bm: BinaryMessage => bm.dataStream.runWith(Sink.ignore)
      }, Source.fromIterator(() => CalmDb.redisClientPool.withClient { rc =>
        rc.keys(s"$cId:*.app").get.flatten.map(x => rc.hgetall1(x).get).map(x => write(x))
      }.take(10).iterator)
        .throttle(1, 1 second, 1, ThrottleMode.Shaping)
        .map(x => TextMessage(x.toString))
    )
    val route = path("course") {
      handleWebSocketMessages(greeter)
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}