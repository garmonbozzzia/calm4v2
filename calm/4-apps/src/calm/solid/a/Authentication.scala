package calm.solid

import org.gbz.Tag._
import Types._

// Auth
trait LoginTag
trait PasswordTag
trait SessionIdTag
//  type Login = String @@ LoginTag
//  type Password = String @@ PasswordTag
//  type SessionId = String @@ SessionIdTag
case object SessionId
case class Credentials(login: Password, password: String @@ Password, sid: String @@ SessionId)


//WebClient
trait HtmlSource[T]
trait JsonSource[T]
trait CourseListTag
object AllCourses extends CourseListTag