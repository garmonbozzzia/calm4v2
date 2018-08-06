package calm.solid

trait CoreModule {
  trait Apply[F[_],A,B] {
    def apply(a:A):B
  }

  object ApplyObject {
    def apply[A,B,F[A] <: Apply[F,A,B]](a:A)(implicit v: F[A] ): B = v(a)
  }
}