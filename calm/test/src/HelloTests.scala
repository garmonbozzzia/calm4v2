import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import org.gbz.Extensions._
import org.gbz.calm.{Authentication, Calm}
import utest._

import scala.concurrent.Await

object HelloTests extends TestSuite{
  import org.gbz.calm.Global._

  override def utestAfterAll()= {
    import scala.concurrent.duration._
    Await.result(system.terminate(), 1 minute)
  }



  val tests = Tests{
    'Sandbox - {
      case class A(a: Int)
      case class B(b: Int)
      implicit val aa = A(2)
      implicit def bb(b: Int) = B(b)
      implicitly[A].a.trace
      implicitly[B](10).b.trace
      implicitly[B](12).b.trace
    }

    'GoogleTest - {
      val a = for{
        result <- Http().singleRequest(Get("https://google.com/"))
        _ <- result.traceWith(_.status).discardEntityBytes().future()
      } yield result
      a.foreach(_.trace)
    }

    'SignIn - {
      for {
        auth <- Authentication.cookie
        result <- Http().singleRequest(Get("https://calm.dhamma.org/en/courses").addHeader(auth))
        _ <- result.traceWith(_.status).discardEntityBytes().future()
      } yield auth
    }

    'Redis - {
      Calm.redisClient.psetex("c4.testKey", 10000, "testValue2")
      Calm.redisClient.get("c4.testKey").trace
      Calm.redisClientPool.withClient(client => {
        Calm.redisClient.psetex("c4.testKey1", 10000, "testValue1")
        Calm.redisClient.get("c4.testKey1").trace
      })
      Calm.redisClientPool.withClient(client => {
        Calm.redisClient.psetex("c4.testKey2", 10000, "testValue2")
        Calm.redisClient.get("c4.testKey2").trace
      })
    }

    'Calm - {
//      val x: CourseList = Calm.http(GetCourseList)
//      val y: CourseList = Calm.redis(GetCourseList)
      import org.gbz.calm.CalmModel._
      Calm.http[CourseList](implicitly[CalmRequest[CourseList]])
        .map(Calm.import2redis)
//        .map(_.courses.mkString("\n").log(logs/"courses.json"))
    }
  }
}