package org.gbz.calm

import akka.stream.scaladsl.{Sink, Source}
import Global._
import org.gbz.ExtUtils._
import org.gbz.calm.model.{AppListRequests, CourseListRequest, CourseRecord}

/* Created on 05.05.18 */

object CalmApps extends App {
  def courseTypes = Calm.redisCourseList.c10d.dullabha.courses.map(_.status).distinct

  import scala.concurrent.duration._

  args.lift(0) match {
    case Some("loadCourses") =>
      CourseListRequest.trace("Loading...").http
        .onComplete{ x =>
          x.trace("Done").traceWith(_.map(_.courses.zipWithIndex.map(_.swap).mkString("\n")))
          system.terminate()
        }

    case Some("loadAllApps") =>
      def loadApps = {
        val c10ds = Calm.redisCourseList.c10d.dullabha.finished
        Source.fromIterator(() => c10ds.courses.iterator)
          .map(c => AppListRequests.fromJson(c.cId))
          .mapAsync(1)(_.http)
          .runForeach(x => CalmDb.update(x))
      }
      def tick = Source.tick(0 second, 1 minute, courses.courses)
        .map(_.trace)
        .mapConcat[CourseRecord](toImmutable)
        //    .mapAsync(1)(Calm.http)
        .runForeach(x => CalmDb.update(x))
      lazy val courses = Calm.redisCourseList.all
      Source.fromIterator(() => courses.courses.iterator)
        .map(_.traceWith(_.cId))
        .mapAsync(1)(org.gbz.calm.CourseData2.update)
        .runWith(Sink.ignore).map(_=>"Done".trace)

    case None => "Enter command as as argument"

  }
}

