package calm.solid

import wvlet.airframe.bind

object SandboxObjects{
  trait Foo[T]{
    def foo(t:T): String
  }

  object Foo {
    def apply[T](implicit foo: Foo[T]): Foo[T] = foo
  }
  trait FooAppSettings{
    implicit val foo: Foo[Int] = bind[Foo[Int]]((_ => "A"):Foo[Int])
  }

  trait Bindings {
    val settings = bind[FooAppSettings]
  }

  trait Bar extends Bindings{
    import settings._
    val bar = Foo[Int].foo(10)
  }
}
