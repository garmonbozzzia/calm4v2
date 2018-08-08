package calm.solid

trait StorageCoreModule {
  this: CommonCoreModule =>

  def write[A:Writer](a:A): Unit = Writer[A].apply(a)
  trait Writer[A] extends Apply[A,Unit]
  object Writer extends Instance[Writer]

  def read[B,A:Curry[Reader,B]#L](a:A): Option[B] = Reader[A,B].apply(a)
  trait Reader[A,B] extends Apply[A,Option[B]]
  object Reader extends Instance2[Reader]

  def read[A:ReaderSingle]: Option[A] = ReaderSingle[A].value
  trait ReaderSingle[A] extends Value[Option[A]]
  object ReaderSingle extends Instance[ReaderSingle]{
    def pure[A](v: => Option[A]): ReaderSingle[A] = new ReaderSingle[A] {
      def value: Option[A] = v
    }
  }
}
