import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.stream.scaladsl.Source
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
      Calm.redisClient.hmset("_", Map("a"-> 0, "b" -> 0))
      Calm.redisClient.hmset("_", Map("c"-> 1, "b" -> 1))
      Calm.redisClient.hgetall1("_").trace
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
        .mapAsync(1)(Calm.http)
        .runForeach(x => CalmDb.export(x.traceWith(_.course_id)))
        //.map(_ => Calm.redisClient.keys("c4:a:*").get.size.trace)
    }

    'LoadApps - {
      val c10ds = Calm.redisCourseList.c10d.dullabha.finished
      Source.fromIterator(() => c10ds.courses.iterator)
        .mapAsync(1)(Calm.http)
        .runForeach(x => CalmDb.export(x.traceWith(_.course_id)))
        .map(_ => Calm.redisClient.keys("c4:a:*").get.size.trace)
    }

    'StatsByCourses - {
      val allApps = Calm.redisAllApps
      allApps.apps.groupBy(_.cId).map(_._2.size).trace
    }

    'Stats - {
      Stats.run
    }

    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL._
    'HtmlImport - {
      val fields = List(
        "app_rcvd",
        "display_id",
        "birth_date",
        "phone_home",
        "phone_mobile",
        "email",
        "incremented_enrolled_quota_at",
        "decremented_enrolled_quota_at"
      )

      val fieldsRename = Map(
        "app_rcvd" -> "received",
        "phone_home" -> "phoneHome",
        "phone_mobile" -> "phoneMobile",
        "incremented_enrolled_quota_at" -> "enrolled",
        "decremented_enrolled_quota_at" -> "dismissed"
      )

      val course = Calm.redisCourseList.c10d.dullabha.finished.courses.head.trace
      for {
        courseData <- Calm.html(course)
        apps = (browser.parseString(courseData) >> elementList("tbody"))
          .last.>>(elementList("tr")).map(_.>>(elementList("td[id]")).map(x => x.attr("id") -> x.text).toMap)
          .map(_.filterKeys(fields.contains).map { case (k, v) => fieldsRename.getOrElse(k, k) -> v })
        kvs = apps.map(app => s"*:*-${app("display_id")}.app" -> app)
        _ = CalmDb.update(kvs).trace
//        _ = Calm.redisClientPool.withClient { rc =>
//          apps.foreach(app => rc.hmset(s"${course.cId}:${app("display_id")}.app", app))
////          apps.foreach{app => s"${course.cId}:${app("display_id")}.app".trace; app.trace}
        } yield ()
    }

    'HtmlStats - {
      val fields = List(
        "app_rcvd",
        "display_id",
        "note_alerts",
        "full_time",
        "birth_date",
        "phone_home",
        "phone_mobile",
        "email",
        "friends_family",
        "incremented_enrolled_quota_at",
        "decremented_enrolled_quota_at"
      )
      val course = Calm.redisCourseList.c10d.dullabha.finished.courses.head.trace
      for{
        courseData <- Calm.html(course)
        apps = (browser.parseString(courseData) >> elementList("tbody"))
          .last.>>(elementList("tr")).map(_.>>(elementList("td[id]")).map(x => x.attr("id") -> x.text).toMap)
        _ = apps.flatten.groupBy(x => x._1)
          .mapValues(_.map(_._2).distinct)
            .traceWith(x => x("actions"))
          .mapValues(_.size)
          .mkString("\n").trace.log
//        _ = apps.map(x => x("")).groupBy(x => x).mapValues(_.size).mkString("\n").trace.log
      } yield ()
    }

    'Html - {
      val fields = List(
        "app_rcvd",
        "display_id",
        "birth_date",
        "phone_home",
        "phone_mobile",
        "email",
        "friends_family",
        "incremented_enrolled_quota_at",
        "decremented_enrolled_quota_at"
      )
      val course = Calm.redisCourseList.c10d.dullabha.finished.courses.head
      for{
        courseData <- Calm.html(course)
        apps = (browser.parseString(courseData) >> elementList("tbody"))
          .last.>>(elementList("tr")).map(_.>>(elementList("td[id]")).map(x => x.attr("id") -> x.text).toMap)
          .log
        //(browser.parseString(courseData) >> elementList("tbody")).map(_.outerHtml.take(100)).trace
        b = apps.map(_("decremented_enrolled_quota_at")).filter(_ != "").size.trace
//        c = apps.map(_("incremented_enrolled_quota_at")).filter(_ != "").sorted.mkString("\n").trace
        d = apps.map(_("app_rcvd")).sorted.mkString("\n").trace
      } yield d
    }

    'CourseData - {
      val course = Calm.redisCourseList.c10d.dullabha.finished.courses.head
      for {
        courseData <- Calm.http(course)
        _ = CalmDb.update(courseData)
        //allApps = Calm.loadCourseApps(course.cId)
      } yield courseData.all.mkString("\n").log
    }

    'BandwithTest - {
      Source.repeat(Get("http://80.211.27.151/ru/schedules/schdullabha"))
          .take(1000)
        .mapAsync(1000)(Http().singleRequest(_))
        .map(_.entity.discardBytes())
        .runForeach(_.trace(System.currentTimeMillis()))
    }

    'map2cc - {
      import scala.reflect._
      import scala.reflect.runtime.universe._

      def fromMap[T: TypeTag: ClassTag](m: Map[String,_]) = {
        val rm = runtimeMirror(classTag[T].runtimeClass.getClassLoader)
        val classTest = typeOf[T].typeSymbol.asClass
        val classMirror = rm.reflectClass(classTest)
        val constructor = typeOf[T].decl(termNames.CONSTRUCTOR).asMethod
        val constructorMirror = classMirror.reflectConstructor(constructor)
        val constructorArgs = constructor.paramLists.flatten.map( (param: Symbol) => {
          val paramName = param.name.toString
          if(param.typeSignature <:< typeOf[Option[Any]])
            m.get(paramName)
          else
            m.getOrElse(paramName, throw new IllegalArgumentException("Map is missing required parameter named " + paramName))
        })

        constructorMirror(constructorArgs:_*).asInstanceOf[T]
      }

      fromMap[A](Map("a" -> "A")).trace
    }
  }
}