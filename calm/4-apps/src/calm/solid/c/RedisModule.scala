package calm.solid

import com.redis._
import org.gbz.utils.log.Log._

trait RedisModule extends LogSupport {
  this: CoreModule with EntitiesModule =>
  val redisClient = new RedisClient("localhost", 6379, 2)
  val redisClientPool = new RedisClientPool("localhost", 6379, 8, 2)

  type Serializer[X] = Converter[X, RedisMap]

  implicit def converterW[A: KeyMaker : Serializer]: Writer[A] =
    a => redisClient.hmset(KeyMaker[A].apply(a), implicitly[Serializer[A]].apply(a))

  type Deserializer[X] = Converter[RedisMap, X]

  implicit def converterR[A: KeyMaker, B: Deserializer]: Reader[A, B] =
    a => redisClient.hgetall1(KeyMaker[A].apply(a)).map(implicitly[Deserializer[B]].apply(_))

  type SerializerS[X] = Converter[X, RedisString]

  implicit def converterWS[A: KeyMaker : SerializerS]: Writer[A] =
    a => redisClient.set(KeyMaker[A].apply(a), implicitly[SerializerS[A]].apply(a))

  type DeserializerS[X] = Converter[RedisString, X]

  implicit def converterRS[A: KeyMaker, B: DeserializerS]: Reader[A, B] =
    a => redisClient.get(KeyMaker[A].apply(a)).map(implicitly[DeserializerS[B]].apply(_))

}
