package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import org.gbz.Tag._
import org.gbz.Global._

import scala.concurrent.Future

trait AuthModule extends AuthCoreModule {

  implicit val authStorage = inMemoAuthStorage
  implicit val authClient = calm4AuthClient

  def inMemoAuthStorage: AuthStorage = new AuthStorage {
    var value: Option[SessionId] = None
    override def read(): Option[SessionId] = value
    override def write(session: SessionId): Unit = value = Some(session)
  }

  implicit def credintals: Credentials = {
    val Seq(login, password, oldSessionId) = scala.io.Source.fromFile("data/login2").getLines().toSeq
    Credentials(login.@@[LoginTag], password, oldSessionId)
  }

  def calm4AuthClient(implicit credentials: Credentials): AuthClient = new AuthClient {
    private val expiredId = Cookie("_sso_session", credentials.sid)
    private val signInRequest =
      Post("https://calm.dhamma.org/en/users/sign_in", FormData(
        "user[login]" -> credentials.login,
        "user[password]" -> credentials.password,
        "commit" -> "Log In")).addHeader(expiredId)

    override def signIn : Future[String@@SessionIdTag] = for {
      response <- Http().singleRequest(signInRequest)
      _ <- response.discardEntityBytes().future()
      session <- response.header[`Set-Cookie`].map(_.cookie.value)
        .fold(Future.failed[String](new Exception("Login failed")))(Future.successful)
    } yield session
  }
}