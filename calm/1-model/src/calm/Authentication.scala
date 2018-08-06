package calm

import org.gbz.Tag._

object Authentication {
  trait LoginTag
  trait PasswordTag
  trait SessionIdTag
  type Login = String @@ LoginTag
  type Password = String @@ PasswordTag
  type SessionId = String @@ SessionIdTag

  case object SessionId
}
import Authentication._
case class Credentials(login: Login, password: Password, sid: SessionId)
