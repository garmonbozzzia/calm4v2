package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Cookie
import akka.util.ByteString
import ammonite.ops.{Path => FPath, read => readFile, _}
import org.gbz.Global.{materializer, system, _}
import org.gbz.Tag._
import org.gbz.utils.log.Log._
import utest._
import wvlet.airframe._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpHeader, Uri}

import scala.collection.immutable.{Seq => ISeq}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

trait Key[T]{
  def key(t:T): FPath
}
object Key {
  def apply[T](implicit key: Key[T]): Key[T] = key
}
trait Validator[T]
object Validator{
  def apply[T](implicit v: Validator[T]): Validator[T] = v
  def pure[T]: Validator[T] = new Validator[T] {}
}

trait WebClientA[A,B]{
  def get(t:A):Future[String@@B]
}

object WebClientA{
  def apply[A, B](implicit wc: WebClientA[A,B]): WebClientA[A,B] = wc
  def pure[A,B](f: A => Future[String@@B]): WebClientA[A, B] = t => f(t)
  def cached[A:Key,B:Validator]: WebClientA[A,B] = pure( request =>
    Try(request)
      .map(Key[A].key)
      .map(readFile)
      .map(_.@@[B])
      .fold(Future.failed, Future.successful)
  )
  def calm4[A:CalmUri,B:CalmHeaders]: WebClientA[A,B] =
      pure[A,B]( calmRequest =>
        for {
          auth <- bind[AuthManager].sessionId.map(Cookie("_sso_session", _))
          //auth <- Bind[AuthManager].sessionId.map(Cookie("_sso_session", _))
          request = Get(CalmUri[A].uri(calmRequest)).withHeaders(auth +: CalmHeaders[B].headers)
          response <- Http().singleRequest(request)
          json <- response.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
        } yield json.utf8String.@@[B]
    )
}

//trait Factory[T]{
//  def t: T
//}
//object Factory{
//  def pure()
//}

trait WebClientSettings {
  implicit val keyInt: Key[Int] = bind[Key[Int]]
  implicit val vldStr: Validator[String] = bind[Validator[String]](Validator.pure[String])
  implicit val cuClt: CalmUri[CourseListTag] = bind[CalmUri[CourseListTag]](CalmUri.coursesUri)
  implicit val cuInt: CalmUri[Int] = bind[CalmUri[Int]]
  implicit val chStr: CalmHeaders[String] = bind[CalmHeaders[String]](CalmHeaders.pure[String](ISeq.empty[HttpHeader]))

//  implicit val wc: WebClientA[Int, String] = bind[WebClientA[Int,String]](WebClientA.cached)
//  implicit val wcIc: WebClientA[Int, String] = WebClientA.cached
//  implicit def wcCached[A:Key,B:Validator]: WebClientA[A,B] = WebClientA.cached[A,B]
  implicit def wcCalm[A:CalmUri,B:CalmHeaders]: WebClientA[A,B] = WebClientA.calm4[A,B]


}

trait AppSettings{
  val wcSettings = bind[WebClientSettings]
}

trait WebClientApp extends AppSettings {
  import wcSettings._
  val kInt = Key[Int]
  val wc = WebClientA[Int,String]//(wcSettings.wc)
}

object Develop extends TestSuite with LogSupport with Designs {
  override def tests = Tests{
    'Auth{

    }
    * - {
      val wc = newDesign
//        .bind[Validator[String]].toInstance(Validator.pure[String])
        .bind[Key[Int]].toInstance(_ => pwd/'data/'login)
        .bind[CalmUri[Int]].toInstance(_ => Uri./)
//        .bind[WebClientA[Int,String]]
//        .toInstanceProvider[Key[Int], Validator[String]](WebClientA.cached(_,_))
        .newSession.build[WebClientApp].wc
      wc.get(10)
    }
  }

  override def utestAfterAll(): Unit = {
    super.utestAfterAll()
    Http().shutdownAllConnectionPools().andThen { case _ =>
      materializer.shutdown()
      system.terminate()
    }
    Await.ready(system.terminate(),10 seconds)
  }
}
