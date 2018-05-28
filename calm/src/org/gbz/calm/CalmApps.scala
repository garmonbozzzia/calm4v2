package org.gbz.calm

import akka.stream.scaladsl.{Sink, Source}
import Global._
import org.gbz.Extensions._
import org.gbz.calm.CalmModel.CourseRecord

/* Created on 05.05.18 */

object CalmApps extends App {
  def loadApps = {
    val c10ds = Calm.redisCourseList.c10d.dullabha.finished
    Source.fromIterator(() => c10ds.courses.iterator)
      .map(_.traceWith(_.cId).dataRequest1)
      .mapAsync(1)(Calm.http)
      .runForeach(x => CalmDb.update(x))
  }

  def courseTypes = Calm.redisCourseList.c10d.dullabha.courses.map(_.status).distinct

  val courses = Calm.redisCourseList.all
  import scala.concurrent.duration._

  def tick = Source.tick(0 second, 1 minute, courses.courses)
    .map(_.trace)
    .mapConcat[CourseRecord](toImmutable)
//    .mapAsync(1)(Calm.http)
    .runForeach(x => CalmDb.update(x))

  Source.fromIterator(() => courses.courses.iterator)
    .map(_.traceWith(_.cId))
    .mapAsync(1)(org.gbz.calm.CourseData2.update)
    .runWith(Sink.ignore).map(_=>"Done".trace)
}

