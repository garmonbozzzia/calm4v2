package calm.solid

trait CoreModule {
  trait Apply[F[_],A,B] {
    def apply(a:A):B
  }
}
