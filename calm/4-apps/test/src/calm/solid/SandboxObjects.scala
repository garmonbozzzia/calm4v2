package calm.solid

import wvlet.airframe.bind

object SandboxObjects {

  case class Person(name: String, age: Int)

  val Alice = Person("Alice", 25)
  val Bob = Person("Bob", 20)

  trait Foo[T] {
    def apply(f: Int => String): String = f(10)

    def foo(t: T): String
  }

  object Foo {
    def apply[T](implicit foo: Foo[T]): Foo[T] = foo
  }

  trait FooAppSettings {
    implicit val foo: Foo[Int] = bind[Foo[Int]]((_ => "A"): Foo[Int])
  }

  trait Bindings {
    val settings = bind[FooAppSettings]
  }

  trait Bar extends Bindings {

    import settings._

    val bar = Foo[Int].foo(10)
  }

  val a: Foo[String] = _ => ""
  val b: String = a { x => x.toString
  }

  //  final def at[T]
  trait Case[A, B] {
    def apply(a: A): B
  }

  val caseInt = new Case[Int, String] {
    override def apply(a: Int) = a.toString
  }

  def fooo: Int => String = _.toString

  caseInt(10)

  def convert[A] = new {
    def apply[B](f: A => B) =
      new Case[A, B] {
        override def apply(a: A) = f(a)
      }
  }

  val res = convert(fooo)
}
