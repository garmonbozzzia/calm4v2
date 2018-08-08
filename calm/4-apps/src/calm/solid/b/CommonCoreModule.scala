package calm.solid

trait CommonCoreModule {
  trait Curry[F[_,_],A]{
    type L[X] = F[X,A]
    type R[X] = F[A,X]
  }
  trait Apply[A,B] {
    def apply(a:A):B
  }

  trait Value[T]{
    def value:T
  }

  trait Instance[F[_]]{
    def apply[A](implicit f:F[A]): F[A] = f
  }

  trait Instance2[F[_,_]]{
    def apply[A,B](implicit f:F[A,B]): F[A,B] = f
  }
}

trait CoreModule extends
  CommonCoreModule with AuthCoreModule with CalmUriCoreModule with WebCoreModule {
  this: EntitiesModule =>
}
