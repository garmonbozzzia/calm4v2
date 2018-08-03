package calm

import calm.Authentication.SessionId
import org.gbz.ExtUtils._
import org.gbz.Global._
import org.gbz.utils.log.Log.{LogSupport, _}
import wvlet.airframe.bind

import scala.concurrent.Future

trait Storage[T] {
  def write(obj: T): Unit
  def read[U](key: U): Option[T]
}

trait AuthClient {
  def signIn: Future[SessionId]
}

class AuthManager extends LogSupport {
  private val storage = bind[Storage[SessionId]]
  def sessionId: Future[SessionId] =
    storage.read(SessionId.logDebug).fold(bind[AuthClient].signIn.map(_ <<< storage.write))(Future(_))
}
