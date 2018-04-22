import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.stream.scaladsl.Source
import org.gbz.Extensions._
import org.gbz.calm.{Authentication, Calm}
import utest._

import scala.concurrent.Await

case class A(a:String){}

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

    object Timer {
      def apply[T,R](expr: => T)(cont: (Long,T) => R) = {
        val start = System.currentTimeMillis
        val res = expr
        val time = System.currentTimeMillis - start
        cont(time,res)
      }
    }
    'Timer - {
      Timer(Thread.sleep(1000)){(t,r) => println(t)}
    }
    import org.gbz.calm.CalmModel._
    'Calm - {
      val request = implicitly[CalmRequest[CourseList]]
      Timer(Calm.loadCourseList.c10d.dullabha.finished)((t,cs) =>
        cs.trace(t).courses.sortBy(_.start).mkString("\n").log)
    }

    'CourseList - {
      val request = implicitly[CalmRequest[CourseList]]
      for {
              httpCourseList <- Calm.http[CourseList](request)
              _ = Calm.export2redis(httpCourseList)
              redisCourseList = Calm.loadCourseList
      } yield redisCourseList.courses.size.trace == httpCourseList.courses.size.trace
    }

    'LoadApps - {
      val courses = Calm.loadCourseList.c10d.dullabha.finished.courses
      Source.fromIterator(() => courses.iterator)
          //.take(5)
        .mapAsync(1)(Calm.http)
        .runForeach(x => Calm.export2redis(x.traceWith(_.course_id)))
        .map(_ => Calm.redisClient.keys("c4.a.*").get.size.trace)
    }

    'Stats - {
      val allApps = Calm.loadAllApps
      //allApps.apps.foreach(_.log)
      allApps.states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace

      allApps.s.states.map{case(x,y) => f"$x%25s: $y"}.mkString("\n").trace
      allApps.s.complete.apps.count(_.nServe == 0).trace
      allApps.s.complete.apps.count(x => x.nServe == 0 && x.nSat == 1).trace
      allApps.s.complete.apps.filter(x => x.nServe == 0 && x.nSat == 1)
        .map(x=> s"${x.familyName} ${x.givenName}").mkString("\n")trace

      allApps.ages.map{case (a,c) => f"$a%2s: $c%3s${List.fill((c+3)/4)(":").mkString}"}
      allApps.s.ages.map{case (a,c) => f"$a%2s: $c%3s${List.fill(c)(":").mkString}"}
      allApps.s.ages.map{case (a,c) => f"$a%2s: $c%3s${List.fill(c)(":").mkString}"}
      allApps.s.filter(_.nServe == 0).ages.map{case (a,c) => f"$a%2s: $c%3s${List.fill(c)(":").mkString}"}
        .mkString("\n").trace
      s"  Total: ${allApps.apps.size}".trace
      s"    New: ${allApps.n.apps.size}".trace
      s"    Old: ${allApps.o.apps.size}".trace
      s"Service: ${allApps.s.apps.size}".trace
      s"   Male: ${allApps.m.apps.size}".trace
      s" Female: ${allApps.f.apps.size}".trace

      s"Total Comleted: ${allApps.apps.size}".trace
      s"           New: ${allApps.n.apps.size}".trace
      s"           Old: ${allApps.o.apps.size}".trace
      s"  Old sat once: ${allApps.complete.o.apps.count(_.nSat == 1)}".trace
      s"       Service: ${allApps.s.apps.size}".trace
      s"          Male: ${allApps.m.apps.size}".trace
      s"        Female: ${allApps.f.apps.size}".trace

      allApps.complete.o.apps.count(_.nSat == 1).traceWith(x => s"Old students sat once: $x").trace
      allApps.complete.o.apps.count(_.nServe > 0).traceWith(x => s"Old students already served: $x").trace
    }

    'Html - {
      import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
      import net.ruippeixotog.scalascraper.dsl.DSL._
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
      val course = Calm.loadCourseList.c10d.dullabha.finished.courses.head
      for{
        courseData <- Calm.html(course)
        apps = (browser.parseString(courseData) >> elementList("tbody"))
          .last.>>(elementList("tr")).map(_.>>(elementList("td[id]")).map(x => x.attr("id") -> x.text).toMap)
        //(browser.parseString(courseData) >> elementList("tbody")).map(_.outerHtml.take(100)).trace
        b = apps.map(_("decremented_enrolled_quota_at")).filter(_ != "").size.trace
        c = apps.map(_("incremented_enrolled_quota_at")).filter(_ != "").size.trace
        d = apps.map(_("app_rcvd")).sorted.mkString("\n").trace
      } yield d
    }

    'CourseData - {
      val course = Calm.loadCourseList.c10d.dullabha.finished.courses.head
      for {
        courseData <- Calm.http(course)
        _ = Calm.export2redis(courseData)
        //allApps = Calm.loadCourseApps(course.cId)
      } yield courseData.all.mkString("\n").log
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
            m.get(paramName).getOrElse(throw new IllegalArgumentException("Map is missing required parameter named " + paramName))
        })

        constructorMirror(constructorArgs:_*).asInstanceOf[T]
      }

      fromMap[A](Map("a" -> "A")).trace
    }
  }
}