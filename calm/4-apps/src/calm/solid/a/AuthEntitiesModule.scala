package calm.solid

import org.gbz.Tag.@@

trait AuthEntitiesModule {
  trait LoginTag
  trait PasswordTag
  trait SessionIdTag
  type Login = String @@ LoginTag
  type Password = String @@ PasswordTag
  type SessionId = String @@ SessionIdTag
  case class Credentials(login: Login,
                         password: String @@ Password,
                         sid: String @@ SessionId)
}

trait WebEntityModel {
  trait HtmlContent[T]
  trait JsonContent[T]
}

trait CalmEntitiesModule {
  trait CourseListTag
  trait SearchTag
  trait AppIdTag
  trait CourseIdTag
  trait ReflistTag
  trait InboxTag
  type Search = String @@ SearchTag
  type AppId = Int @@ AppIdTag
  type CourseId = Int @@ CourseIdTag
  type CourseRequest = CourseId
  type AppRequest = (AppId, CourseId)
  trait MessageTag
  type MessageRequest = (Int @@ MessageTag, AppId)
  trait NoteTag
  type NoteRequest = (Int @@ NoteTag, AppId)
  trait ConversationRequestTag
  type ConversationRequest = Int @@ ConversationRequestTag
  trait CourseListRequestTag
  type CourseListRequest = Unit @@ CourseListRequestTag
  trait UriTag[A]
}

trait EntitiesModule
    extends AuthEntitiesModule
    with WebEntityModel
    with CalmEntitiesModule
