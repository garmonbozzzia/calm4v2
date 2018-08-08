package calm.solid

import akka.http.scaladsl.Http
import calm.solid.MockModules._
import org.gbz.Global.{materializer, system, _}
import org.gbz.Tag._
import utest._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.{higherKinds, implicitConversions}


trait StorageCoreModule {
  this: CommonCoreModule =>
  def read[B,A:Curry[Reader,B]#L](a:A):B = Reader[A,B].apply(a)
  def read[A:ReaderSingle]: A = ReaderSingle[A].value
  def write[A:Writer]: Unit
  trait Writer[A] extends Apply[A,Unit]
  trait Reader[A,B] extends Apply[A,B]
  object Reader extends Instance2[Reader]
  object ReaderSingle extends Instance[ReaderSingle]
  trait ReaderSingle[A] extends Value[A]
}

object Develop extends TestSuite with AppModule
  //with MocAuthStorage
  with MocAuthClient
  with MocHtmlSource
  with MocJsonSource
{
  override def tests = Tests{
    'Storage - {

    }
    'Uri - {
      for{
        sid <- sessionId
        res <- json("Игнатьев".@@[SearchTag])
      } yield res
//      uri("Игнатьев".@@[SearchTag])
//      uri[AppRequest](123.@@[AppIdTag] -> 4053.@@[CourseIdTag]).log
//      uri[CourseListRequest](().taggedWith[CourseListRequestTag]).log
//      html("Игнатьев".@@[SearchTag])
    }

    'WebClient - {
//      for{
//        htmlPage <- html(4053)
//        jsonPage <- json(4053)
//      } yield "Done"
//        .log(htmlPage.substring(0,100))
//        .log(jsonPage.substring(0,100))
//        .logWarn("MSG")
      //todo выяснить почему сообщения печатаются в неправильном порядке
    }

    'Storage{
      for{
        _ <- sessionId
        _ <- sessionId
        res <- sessionId
      } yield res
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
