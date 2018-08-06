package stash

import akka.http.scaladsl.model.Uri
import calm.solid.Sandbox.mainDesign
import calm.solid.SandboxObjects.Person
import calm.solid._
import org.gbz.Tag.@@
import utest._

import scala.collection.immutable.{Seq => ISeq}
import scala.concurrent.Future

trait SessionCoreModule{

  object Implicits {
    implicit val ims: Storage[Int, String] =
      MapStorage.pure(collection.mutable.Map.empty[Int,String])
    implicit val kmPs: KeyMaker[Person,Int] = _.age
    implicit val sPs: Serializer[Person,String] = _.name
    implicit val dPs: Deserializer[String,Person] = x => Some(Person(x,0))
  }

  //  trait SingleStorage[V] extends TypesHelper[V]{
  //    var value: Option[V] = None
  //    def write[A:Ser](obj: A)
  //    def read[A:Des]: Option[A] = value.flatMap(Deserializer[V,A])
  //  }

  trait TypesHelper[A] {
    type Ser[X] = Serializer[X,A]
    type Des[X] = Deserializer[A,X]
  }

  trait Storage[K,V]{
    def write[A](a:A)(implicit km:KeyMaker[A,K], ser: Serializer[A,V]): Unit =
      writeWithKey(km(a),ser(a))
    def writeWithKey(k:K, v:V): Unit
    def read(k:K): Option[V]
    def readAny[A,B](a:A)(implicit km: KeyMaker[A,K], des: Deserializer[V,B]): Option[B] =
      read(km(a)).flatMap(des(_))
  }
  object Storage{
    def apply[K,V](implicit s: Storage[K,V]): Storage[K, V] = s
  }

  trait MapStorage[K,V] extends Storage[K,V] {
    val storage: collection.mutable.Map[K,V]
    def writeWithKey(k:K, v:V): Unit = storage.update(k,v)
    def read(k:K): Option[V] = storage.get(k)
  }

  trait KeyMaker[A,B] extends Appliable[A,B]
  object KeyMaker{
    def apply[A,B](a:A)(implicit km: KeyMaker[A,B]):B = km(a)
  }

  implicit def serIdentity[A]: Serializer[A,A] = x => x
  implicit def desIdentity[A]: Deserializer[A,A] = x => Some(x)

  def write[K,V](v:V)(implicit st: Storage[K,V], km: KeyMaker[V,K]): Unit =
    Storage[K,V].writeWithKey(KeyMaker[V,K](v), v)

  def read[K,V](k:K)(implicit st: Storage[K,V]): Option[V] =
    Storage[K,V].read(k)

  implicit def ms[KK, VV, K, V](implicit
                                st: Storage[KK, VV],
                                km: KeyMaker[K, KK],
                                ser: Serializer[V, VV],
                                des: Deserializer[VV, V]
                               ): Storage[K, V] = new Storage[K, V] {
    override def writeWithKey(k: K, v: V): Unit =
      Storage[KK, VV].writeWithKey(KeyMaker[K, KK](k), Serializer[V, VV](v))

    override def read(k: K): Option[V] =
      Storage[KK, VV].read(KeyMaker[K, KK](k)).flatMap(Deserializer[VV, V])
  }

  object MapStorage{
    def pure[K,V](map: collection.mutable.Map[K,V]): MapStorage[K,V] = new MapStorage[K,V] {
      override val storage = map
    }
  }

  implicit def kmIdent[A]: KeyMaker[A,A] = x => x
  trait Serializer[A,B] extends Appliable[A,B]
  object Serializer{
    def apply[A,B](a:A)(implicit s: Serializer[A,B]): B = s(a)
  }

  trait Appliable[A,B]{
    def apply(a:A):B
  }

  //todo apply for Appliable object
  trait Deserializer[A,B] extends Appliable[A,Option[B]]
  object Deserializer{
    def apply[A,B](a:A)(implicit s: Deserializer[A,B]): Option[B] = s(a)
  }
}
trait InMemoStorageModule extends SessionCoreModule {
  //  trait InMemoStorage[T] extends Storage[T] with LogSupport {
  //    private var storage = Map.empty[String, T]
  //    protected def makeKey[A](obj: A): String
  //    override def write(obj: T): Unit = storage = storage.updated(makeKey(obj),obj).logDebug
  //    override def read[A](request: A): Option[T] = storage.get(makeKey(request))
  //  }
  //
  //  class InMemoSessionStorage extends InMemoStorage[Types.SessionId] {
  //    override protected def makeKey[T](obj: T): String = "_sId"
  //  }
}


object Stash extends TestSuite with Designs {
  override def tests = Tests {
    'WebClient - {
      import CalmUri._
      val a: WebClient[CalmUri, CalmHeaders] = mainDesign
        //          .bind[WebClient]
        .newSession.build[WebClient[CalmUri, CalmHeaders]]
    }

    'NewWc - {
      implicit val keyInt: Key[Int] = ???
      implicit val vldStr: Validator[String] = ???
      implicit val cuInt: CalmUri[Int] = ???
      implicit val chStr: CalmHeaders[String] = ???

      implicit def wcIntStr[A: CalmUri, B: CalmHeaders]: WebClientA[A, B] = WebClientA.calm4[A, B]

      val a: Future[String @@ String] = wcIntStr.get(10)
      //WebClientA.calm4[Int,String]
    }
    'SandBox - {
      //Implicits
      //      implicit val key: Key[Int] = _ => pwd
      implicit val vld: Validator[String] = ???
      implicit val cu: CalmUri[Int] = _ => Uri./
      implicit val ch: CalmHeaders[String] = CalmHeaders.pure(ISeq.empty)
      //      val key: Key[Int] = _ => pwd
      //      val key2: Validator[String] = ???
      //      implicit val common: Common[Int,String] = common2
      //      implicit val cmn: Common[Int,String] = UriHeader[Int, String]()
    }

    'WebClient - {
      import CalmUri._
      val a: WebClient[CalmUri, CalmHeaders] = mainDesign
        //          .bind[WebClient]
        .newSession.build[WebClient[CalmUri, CalmHeaders]]
    }

    'NewWc - {
      implicit val keyInt: Key[Int] = ???
      implicit val vldStr: Validator[String] = ???
      implicit val cuInt: CalmUri[Int] = ???
      implicit val chStr: CalmHeaders[String] = ???
      implicit def wcIntStr[A:CalmUri,B:CalmHeaders]: WebClientA[A,B] = WebClientA.calm4[A,B]
      val a: Future[String @@ String] = wcIntStr.get(10)
      //WebClientA.calm4[Int,String]
    }
  }
}
