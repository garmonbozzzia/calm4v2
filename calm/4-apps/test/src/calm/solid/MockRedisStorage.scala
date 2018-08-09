package calm.solid

import calm.solid.SandboxObjects._
import org.gbz.utils.log.Log._
import collection.mutable.{Map => MMap}

trait MockRedisStorage {
  this: AppModule with LogSupport =>

  trait KeyMaker[T] extends Apply[T, String]

  object KeyMaker extends Instance[KeyMaker]

  trait KeyMakerSingle[T] extends Value[String]

  trait Converter[A, B] extends Apply[A, B]

  object Converter extends Instance2[Converter]

  type StringSerializer[A] = Converter[A, String]
  type ListSerializer[A] = Converter[A, List[String]]
  type MapSerializer[A] = Converter[A, Map[String, String]]
  type StringDeserializer[A] = Converter[String, A]
  type ListDeserializer[A] = Converter[List[String], A]
  type MapDeserializer[A] = Converter[Map[String, String], A]
  type MapStorage[X] = MMap[String, X]

  implicit val kmSessionId: KeyMakerSingle[SessionId] =
    new KeyMakerSingle[SessionId] {
      override def value: String = "_sessionId"
    }
  implicit lazy val personKeyMaker: KeyMaker[Person] =
    person => person.name
  implicit lazy val personSer: MapSerializer[Person] = person =>
    Map("name" -> person.name, "age" -> person.age.toString)
  implicit lazy val personDes: MapDeserializer[Person] =
    x => Person(x("name"), x("age").toInt).logWarn

  implicit val redisString: MMap[String, String] = MMap.empty
  implicit val redisMap: MMap[String, Map[String, String]] = MMap.empty
  implicit val redisList: MMap[String, List[String]] = MMap.empty

  implicit def writer[A: KeyMaker, B: Curry[Converter, A]#R: MapStorage]
    : Writer[A] =
    writerHelper[A].apply[B].apply(_)

  private def writerHelper[A: KeyMaker] = new {
    type Serializer[X] = Converter[A, X]

    def apply[B: MapStorage: Serializer]: Writer[A] =
      a =>
        implicitly[MapStorage[B]]
          .update(KeyMaker[A].apply(a), Converter[A, B].apply(a))
          .trace(implicitly[MapStorage[B]])
  }

  implicit def reader[A: KeyMaker, B: MapDeserializer]: Reader[A, B] =
    readerHelper[A].apply[Map[String, String]].apply[B].log

  def readerHelper[A: KeyMaker] = new {
    def apply[C: MapStorage] = new {
      type Deserializer[X] = Converter[C, X]

      def apply[B: Deserializer]: Reader[A, B] =
        a =>
          implicitly[MapStorage[C]].trace
            .get(KeyMaker[A].apply(a).trace)
            .trace
            .map(Converter[C, B].apply)
    }
  }
}
