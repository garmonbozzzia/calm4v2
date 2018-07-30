package org.gbz

import org.gbz.calm.model.CourseListRequest
import utest._
import wvlet.log.LogSupport

/* Created on 13.07.18 */
object ParserTest extends TestSuite with LogSupport {
  import org.gbz.utils.log.Log._
  override def tests = Tests{
    'Old - {

    }

    'A - {
      object Lib {
        var isTest = false
        def mock[T](main: => T)(test: => T): T = if(isTest) test else main
        def foo = mock("Main")("Test")
      }
      object App {
        tests
      }
    }
    'Auth - {
      import scala.concurrent.ExecutionContext.Implicits.global
      CourseListRequest.http.map(_.logWith(_.courses.mkString("\n")))
    }
  }
}
