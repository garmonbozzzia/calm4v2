import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import org.gbz.Extensions._
import org.gbz.calm.CalmModel.CalmRequest
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
    'Calm - {
//      val x: CourseList = Calm.http(GetCourseList)
//      val y: CourseList = Calm.redis(GetCourseList)
      import org.gbz.calm.CalmModel._
      val request = implicitly[CalmRequest[CourseList]]
//      for {
//        httpCourseList <- Calm.http[CourseList](request)
//        _ = Calm.export2redis(httpCourseList.log)
//        redisCourseList = Calm.load(request).log
//      } yield httpCourseList.log
      Timer(Calm.loadCourseList.c10d.dullabha.finished)((t,cs) =>
        cs.trace(t).courses.sortBy(_.start).mkString("\n").log)
//        .map(_.courses.mkString("\n").log(logs/"courses.json"))
//      val courses = GetCourseList(filter).fromCalm ==> Future[CourseList]
      //Calm.save()
//      CourseList(filter).fromCalm ==> Future[CourseList]
//      CourseList(filter).fromRedis
//      CourseList(filter).get

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