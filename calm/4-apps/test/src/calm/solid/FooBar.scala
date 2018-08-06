package calm.solid

object FooBar {

  trait Baz

  trait Bar[R] {
    type Repr = Baz

    def bar(t: Repr): R
  }

  trait Foo[T] {
    type Repr = Baz

    def foo(t: T): Repr
  }

  object Foo {
    type Aux[T, R] = Foo[T] {type Repr = R}

    def apply[T](implicit foo: Foo[T]): Foo[T] = foo
  }

  object Bar {
    type Aux[T, R] = Bar[R] {type Repr = T}

    def apply[R](implicit bar: Bar[R]): Bar[R] = bar
  }

  object App {
    def foo[T: Foo, R](t: T): Baz = Foo[T].foo(t)

    def bar[T, R: Bar](t: Baz): R = Bar[R].bar(t)

    def foobar[T: Foo, R: Bar](t: T): R = Bar[R].bar(Foo[T].foo(t))
  }

}
