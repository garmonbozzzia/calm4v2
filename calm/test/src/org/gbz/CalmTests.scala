package org.gbz

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.stream.scaladsl.{Sink, Source}
import org.gbz.ExtUtils._
import org.gbz.calm._
import org.gbz.calm.model.{AppListRequests, CourseListRequest}
import org.gbz.utils.log.Log._
import utest._

import scala.concurrent.Await

object CalmTests extends TestSuite{
  import org.gbz.calm.Global._

  override def utestAfterAll(): Unit = {
    import scala.concurrent.duration._
    Await.result(system.terminate(), 1 minute)
  }

  val tests = Tests{
    'SignIn - {
      import wvlet.airframe._
      val auth = newDesign.bind[SessionStorage].toInstance(SessionStorage.inMemory).newSession.build[Authentication]
        for {
          auth <- auth.cookie //Authentication().cookie
          result <- Http().singleRequest(Get("https://calm.dhamma.org/en/courses").addHeader(auth))
          _ <- result.traceWith(_.status).discardEntityBytes().future()
        } yield auth

    }

    'FindForm - {
      CalmDb.redisClientPool.withClient(rc => {
        rc.keys("*.app").get.flatten.map(rc.hgetall1(_)).map(_.get).filter(_("familyName").contains("Кир")).mkString("\n").trace
      })
    }
    'Calm - {
      val request = CourseListRequest
      Timer(Calm.redisCourseList.c10d.dullabha.finished)(_.trace)(cs =>
        cs.courses.sortBy(_.start.toString).mkString("\n"))
    }

    'CourseList - {
      val request = CourseListRequest
      for {
              httpCourseList <- request.http
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
        .map(c => AppListRequests.fromJson(c.cId))
        .mapAsync(1)(_.http)
        .runForeach(x => CalmDb.export(x))
        //.map(_ => Calm.redisClient.keys("c4:a:*").get.size.trace)
    }

    'LoadApps - {
      val c10ds = Calm.redisCourseList.c10d.dullabha.finished
      Source.fromIterator(() => c10ds.courses.iterator)
        .map(c => AppListRequests.fromJson(c.cId))
        .mapAsync(1)(_.http)
        .runForeach(x => CalmDb.update(x))
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
        courseData <- AppListRequests.merged(course.cId)
        _ = CalmDb.update(courseData)
      } yield courseData.apps.mkString("\n")
    }
  }
}