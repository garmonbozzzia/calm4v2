package calm.solid

trait CommonCoreModule {
  trait Apply[F[_],A,B] {
    def apply(a:A):B
  }
}

trait CoreModule extends
  CommonCoreModule with AuthCoreModule with CalmUriCoreModule with WebCoreModule {
  this: EntitiesModule =>
}
