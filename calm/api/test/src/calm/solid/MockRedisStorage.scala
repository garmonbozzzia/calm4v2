package calm.solid

import calm.solid.SandboxObjects._
import javax.xml.crypto.dsig.keyinfo.KeyValue
import org.gbz.Tag._
import org.gbz.utils.log.Log._

import collection.mutable.{Map => MMap}

trait MockRedisStorage {
  this: AppModule with LogSupport =>

//  type StringSerializer[A] = Converter[A, String]
//  type ListSerializer[A] = Converter[A, List[String]]
  type MapSerializer[A] = Converter[A, RedisMap]
//  type StringDeserializer[A] = Converter[String, A]
//  type ListDeserializer[A] = Converter[List[String], A]
  type MapDeserializer[A] = Converter[RedisMap, A]
//  type MapStorage[X] = MMap[String, X]

  type MapStorage[X] = MMap[RedisKey, X]

  implicit val kmSessionId: KeyMakerSingle[SessionId] =
    new KeyMakerSingle[SessionId] {
      override def value = "_sessionId"
    }
  implicit lazy val personKeyMaker: KeyMaker[Person] =
    person => person.name
  implicit lazy val personSer: MapSerializer[Person] = person =>
    Map("name" -> person.name, "age" -> person.age.toString)
  implicit lazy val personDes: MapDeserializer[Person] =
    x => Person(x("name"), x("age").toInt).logWarn

  implicit val redisString: MMap[RedisKey, RedisString] = MMap.empty
  implicit val redisMap: MMap[RedisKey, RedisMap] = MMap.empty
  implicit val redisList: MMap[RedisKey, List[String]] = MMap.empty

//  implicitly[MapStorage[RedisMap]]
//  implicitly[MMap[RedisKey,RedisMap]]

  type RedisReader[X] = Reader[RedisKey, X]
//  type RedisWriter[X] = Writer[RedisKey, X]
//  implicit val redisStringR: Reader[RedisKey, RedisString] =
//    redisString.get
//  implicit val redisMapR: Reader[RedisKey, RedisMap] =
//    redisMap.get
//  implicit val redisListR: Reader[RedisKey, RedisList] =
//    redisList.get(_).map(_.taggedWith[RedisValueTag])

  type WriterKV[A] = KeyValue => Writer[A]

  def redisStringW(key: RedisKey): Writer[RedisString] =
    a => redisString.update(key, a)
  def redisMapW(key: RedisKey): Writer[RedisMap] =
    a => redisMap.update(key, a)
  def redisListW(key: RedisKey): Writer[RedisList] =
    a => redisList.update(key, a)

//  def ww[A:KeyMaker](a:A)

//  def combine[A,B](writer: Writer[A]): Writer[B] = ???

//  implicit def writer[A: KeyMaker, B: Curry[Converter, A]#R: MapStorage]
//    : Writer[A] =
//    writerHelper[A].apply[B].apply(_)
//
//  private def writerHelper[A: KeyMaker] = new {
//    type Serializer[X] = Converter[A, X]
//
//    def apply[B: MapStorage: Serializer]: Writer[A] =
//      a =>
//        implicitly[MapStorage[B]]
//          .update(KeyMaker[A].apply(a), Converter[A, B].apply(a))
//          .trace(implicitly[MapStorage[B]])
//  }

//  def writerA[A:KeyMaker, B: Curry[Converter, A]#R: MapStorage]

  def writer[A: KeyMaker, B: Curry[Converter, A]#R: MapStorage]
  : Writer[A] =
    writerHelper[A].apply[B].apply(_)

  def writerHelper[A: KeyMaker] = new {
    type Serializer[X] = Converter[A, X]

    def apply[B: MapStorage: Serializer]: Writer[A] =
      a =>
        implicitly[MapStorage[B]]
          .update(KeyMaker[A].apply(a), Converter[A, B].apply(a))
  }

   def reader[A: KeyMaker, B: MapDeserializer]: Reader[A, B] =
    readerHelper[A].apply[RedisMap].apply[B].log

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
