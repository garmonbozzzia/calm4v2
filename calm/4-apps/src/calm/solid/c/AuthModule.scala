package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import org.gbz.Tag._
import org.gbz.Global._
import org.gbz.utils.log.Log._

import scala.concurrent.Future

trait AuthModule {
  this: AuthCoreModule with AuthEntitiesModule =>
  implicit lazy val authStorage: AuthStorage = inMemoAuthStorage
  implicit lazy val authClient: AuthManager @@ NoStorage = calm4AuthClient
  implicit lazy val authManager: AuthManager@@Default = withStorage(authClient)

  def inMemoAuthStorage: AuthStorage = new AuthStorage with LogSupport {
    var value: Option[SessionId] = None
    override def read(): Option[SessionId] = value.logWith(s => s"Read: $s")
    override def write(session: SessionId): Unit = value = Some(session).logWith(s => s"Wrote: $s")
  }

  def calm4AuthClient(implicit credentials: Credentials): AuthManager@@NoStorage = {
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
