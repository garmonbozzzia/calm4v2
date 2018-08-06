package calm.solid

import org.gbz.Tag.@@

trait AuthEntitiesModule {
  trait Default
  trait NoStorage
  trait LoginTag
  trait PasswordTag
  trait SessionIdTag
  type Login = String@@LoginTag
  type Password = String@@PasswordTag
  type SessionId = String@@SessionIdTag
  case class Credentials(login: Login, password: String @@ Password, sid: String @@ SessionId)
}