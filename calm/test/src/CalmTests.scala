import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.stream.scaladsl.{Sink, Source}
import org.gbz.Extensions._
import org.gbz.calm.{Authentication, Calm, CalmDb, Stats}
import utest._

import scala.concurrent.Await

case class A(a:String){}

object CalmTests extends TestSuite{
  import org.gbz.calm.Global._

  override def utestAfterAll()= {
    import scala.concurrent.duration._
    Await.result(system.terminate(), 1 minute)
  }

  val tests = Tests{
    'Sandbox - {
      object Enum extends Enumeration {
        val Red = Value("red")
        val Blue = Value("blue")
      }

      Enum.withName("red").trace
      Enum.withName("green")
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

    import org.gbz.calm.CalmModel._
    'Calm - {
      val request = implicitly[CalmRequest[CourseList]]
      Timer(Calm.redisCourseList.c10d.dullabha.finished)(_.trace)(cs =>
        cs.courses.sortBy(_.start).mkString("\n").log)
    }

    'CourseList - {
      val request = implicitly[CalmRequest[CourseList]]
      for {
              httpCourseList <- Calm.http[CourseList](request)
              _ = Calm.redisCourseList.traceWith(x => s"Courses number: ${x.courses.size}")
              _ = CalmDb.update(httpCourseList)
              redisCourseList = Calm.redisCourseList.traceWith(x => s"Courses number: ${x.courses.size}")
      } yield redisCourseList.courses.size.trace == httpCourseList.courses.size.trace
    }

    'NewCourses - {
      def newCourses = Calm.redisCourseList.c10d.dullabha.finished.courses.map(_.cId)
        .diff(Calm.redisAllApps.apps.map(_.cId).distinct)
      newCourses.trace
    }

    'UpdateApps - {
      val c10ds = Calm.redisCourseList.c10d.dullabha.finished.traceWith(_.courses.map(_.cId))
      val newCourses = Calm.redisCourseList.c10d.dullabha.finished.courses.map(_.cId)
        .diff(Calm.redisAllApps.apps.map(_.cId).distinct).trace
      Source.fromIterator(() => c10ds.courses.iterator)
        .filter(x => newCourses.contains(x.cId))
        .map(_.dataRequest1)
        .mapAsync(1)(Calm.http)
        .runForeach(x => CalmDb.export(x.traceWith(_.course_id)))
        //.map(_ => Calm.redisClient.keys("c4:a:*").get.size.trace)
    }

    'LoadApps - {
      val c10ds = Calm.redisCourseList.c10d.dullabha.finished
      Source.fromIterator(() => c10ds.courses.iterator)
        .map(_.dataRequest1)
        .mapAsync(1)(Calm.http)
        .runForeach(x => CalmDb.update(x.traceWith(_.course_id)))
        //.map(_ => Calm.redisClient.keys("c4:a:*").get.size.trace)
    }

    'StatsByCourses - {
      val allApps = Calm.redisAllApps
      allApps.apps.groupBy(_.cId).map(_._2.size).trace
    }

    'Stats - Stats.run

    'Courses - {
      val courses = Calm.redisCourseList
      courses.courses.map(_.venue).distinct
    }

    'TotalUpdate - {
      val courses = Calm.redisCourseList.c10d.ekb.courses
      Source.fromIterator(() => courses.iterator)
          .map(_.traceWith(_.cId))
        .mapAsync(1)(org.gbz.calm.CourseData2.update)
        .runWith(Sink.ignore).map(_=>"Done".trace)
    }

    'CourseData - {
      val course = Calm.redisCourseList.c10d.dullabha.finished.courses.head
      for {
        courseData <- Calm.http(course.dataRequest1)
        _ = CalmDb.update(courseData)
        //allApps = Calm.loadCourseApps(course.cId)
      } yield courseData.all.mkString("\n").log
    }
  }
}