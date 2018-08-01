package calm

import org.gbz.utils.log.Log.LogSupport
import wvlet.airframe.bind
import wvlet.surface.tag._
import org.gbz.ExtUtils._
import org.gbz.Global._
import org.gbz.utils.log.Log._

import scala.concurrent.Future

trait Storage[T] {
  def write(obj: T): Unit
  def read[U](key: U): Option[T]
}

trait AuthClient {
  def signIn: Future[String@@SessionId]
}

class AuthManager extends LogSupport {
  private val storage = bind[Storage[String@@SessionId]]
  def sessionId: Future[String @@ SessionId] =
    storage.read(SessionId.log).fold(bind[AuthClient].signIn.map(_ <<< storage.write))(Future(_))
}
