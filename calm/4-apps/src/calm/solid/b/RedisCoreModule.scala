package calm.solid

trait RedisCoreModule {
  this: CoreModule with EntitiesModule =>
  trait KeyMaker[T] extends Apply[T, RedisKey]

  object KeyMaker extends Instance[KeyMaker]

  trait KeyMakerSingle[T] extends Value[RedisKey]

  trait Converter[A, B] extends Apply[A, B]

  object Converter extends Instance2[Converter]
}
