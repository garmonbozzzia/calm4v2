package calm.solid

import org.gbz.Tag.@@

//todo Refactor LogSupport
//todo How to abstract ApplyObject?

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

trait WebEntityModel  {
  trait HtmlContent[T]
  trait JsonContent[T]
}