package calm

import org.gbz.utils.log.Log.LogSupport
import wvlet.airframe.bind
import wvlet.surface.tag._
import org.gbz.ExtUtils._
import org.gbz.Global._

import scala.concurrent.Future

trait Storage[T] {
  def write(obj: T): Unit
  def read[U](key: U): Option[T]
}
trait SessionStorage extends Storage[String@@SessionId]

trait AuthClient {
  def signIn: Future[String@@SessionId]
}

class AuthManager extends LogSupport {
  private val storage = bind[SessionStorage]
  def sessionId: Future[String @@ SessionId] =
    storage.read(SessionId).fold(bind[AuthClient].signIn.map(_ <<< storage.write))(Future(_))
}
