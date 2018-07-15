package org.gbz

import org.gbz.ExtUtils.Mock
import org.gbz.calm.model.CourseListRequest
import utest._
import wvlet.log.LogSupport

/* Created on 13.07.18 */
object ParserTest extends TestSuite with LogSupport {
  import org.gbz.utils.log.Log._
  override def tests = Tests{
    'Old - {
//      object MockLib {
//        trait Testable {
//          type MainOrMock
//        }
//        trait Main
//        trait Mock
//        trait Mockable[T] {
//          def create[MainOrMock](implicit wire: Creator[T,MainOrMock]) = wire.create
//        }
//        trait Creator[T, MainOrMock]{
//          def create: T
//        }
//        object Creator{
//          def main[T](createFunc: => T ) = new MockLib.Creator[T, Main]{
//            override def create = createFunc}
//          def test[T](createFunc: => T ) = new MockLib.Creator[T, Mock]{
//            override def create = createFunc}
//        }
//      }
//
//      object Lib{
//        import MockLib._
//        def foo: String = mock("Main").or("Test").result
//        trait Foo extends {def foo: String}
//        class MainFoo extends Foo{
//          override def foo = "Main"
//        }
//        class TestFoo extends Foo{
//          override def foo = "Test"
//        }
//        object Foo extends Mockable[Foo]
//
//        object Implicit {
//          implicit val main = Creator.main(new MainFoo)
//          implicit val test = Creator.test(new TestFoo)
//        }
//      }
//
//      object App {
//        object file1 {
//          object Bar extends MockLib.Testable {
//            def bar = Lib.Foo.create[MainOrMock].foo
//          }
//        }
//
//        object file2 {
//          object Baz {
//            def baz = Lib.Foo().foo
//          }
//        }
//      }
//
//      object Test {
//        object Bar {
//          def bar = Lib.Foo().foo
//        }
//      }
//
//
//      App.file1.Bar.bar ==> "Main"
//      Test.Bar.bar ==> "Test"
//
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
      Mock.isTest = true
      CourseListRequest.http.map(_.logWith(_.courses.mkString("\n")))
    }
  }
}
