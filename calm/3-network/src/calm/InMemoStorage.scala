package calm

import org.gbz.utils.log.Log._
import wvlet.surface.tag.@@

trait InMemoStorage[T] extends Storage[T] with LogSupport {
  private var storage = Map.empty[String, T]
  protected def key[K](obj: K): String
  override def write(obj: T): Unit = storage = storage.updated(key(obj),obj).log
  override def read[K](request: K): Option[T] = storage.get(key(request))
}

class InMemoSessionStorage extends InMemoStorage[String@@SessionId] {
  override protected def key[T](obj: T): String = "_sId"
}
