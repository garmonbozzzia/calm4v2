package org.gbz

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import org.gbz.calm.Global.system
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.Await


object Sandbox extends TestSuite{

  override def utestAfterAll(): Unit = {
    import scala.concurrent.duration._
    Await.result(system.terminate(), 1 minute)
  }
  import org.gbz.calm.Global._

  val tests = Tests {
    'Sandbox - {
      object Enum extends Enumeration {
        val Red = Value("red")
        val Blue = Value("blue")
      }

      Enum.withName("red").trace.id.trace
      Enum(Enum.withName("blue").trace.id.trace).trace
//      Enum.withName("green")
    }

    'GoogleTest - {
      val a = for{
        result <- Http().singleRequest(Get("https://google.com/"))
        _ <- result.traceWith(_.status).discardEntityBytes().future()
      } yield result
      a.foreach(_.trace)
    }
  }
}
