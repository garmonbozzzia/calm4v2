package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import org.gbz.Tag._
import org.gbz.Global._
import org.gbz.utils.log.Log._

import scala.concurrent.Future
import org.gbz.ExtUtils._

trait AuthModule {
  this: AuthCoreModule with AuthEntitiesModule with StorageCoreModule =>

  implicit lazy val sessionReader: ReaderSingle[SessionId] = ???
  implicit lazy val sessionWriter: Writer[SessionId] = ???

  def withStorage(ac:AuthManager)(implicit
    writer: Writer[SessionId], reader:ReaderSingle[SessionId]): AuthManager =
    AuthManager.pure(reader.value.fold(ac.value.map(_ <<< writer.apply))(Future(_)))

  lazy val authClient: AuthManager = calm4AuthClient
  implicit lazy val authManager: AuthManager = withStorage(authClient)

  def calm4AuthClient(implicit credentials: Credentials): AuthManager = {
    val expiredId = Cookie("_sso_session", credentials.sid)
    val signInRequest =
      Post("https://calm.dhamma.org/en/users/sign_in", FormData(
        "user[login]" -> credentials.login,
        "user[password]" -> credentials.password,
        "commit" -> "Log In")).addHeader(expiredId)
    AuthManager.pure(for {
      response <- Http().singleRequest(signInRequest)
      _ <- response.discardEntityBytes().future()
      session <- response.header[`Set-Cookie`].map(_.cookie.value)
        .fold(Future.failed[String](new Exception("Login failed")))(Future.successful)
    } yield session)
  }

  implicit def credentials: Credentials = {
    val Seq(login, password, oldSessionId) = scala.io.Source.fromFile("data/login2").getLines().toSeq
    Credentials(login.@@[LoginTag], password, oldSessionId)
  }
}
