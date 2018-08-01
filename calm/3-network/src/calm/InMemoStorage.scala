package calm

import wvlet.surface.tag.@@

class InMemoStorage extends SessionStorage {
  private var storage = Map.empty[String, String@@SessionId]
  private def key[T,K](obj: T): String = "sId"
  override def write(obj: String@@SessionId) = storage = storage.updated(key(obj),obj)
  override def read[T](request: T): Option[String@@SessionId] = storage.get(key(request))
}
