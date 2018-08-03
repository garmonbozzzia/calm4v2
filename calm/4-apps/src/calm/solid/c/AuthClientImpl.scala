package calm.solid

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import org.gbz.Global._
import org.gbz.Tag._
import org.gbz.utils.log.Log._
import wvlet.airframe.bind
import scala.concurrent.Future

import Types._

object AuthClientImpl extends LogSupport {
  def defaultCredentials: Credentials = {
    val Seq(login, password, oldSessionId) = scala.io.Source.fromFile("data/login2").getLines().toSeq
//    Credentials(login.@@[LoginTag], password.@@[PasswordTag], oldSessionId.@@[SessionIdTag])
    Credentials(login, password, oldSessionId).logDebug
  }
}
trait AuthClientImpl extends AuthClient with LogSupport {

  val credentials = bind[Credentials]

  private val expiredId = Cookie("_sso_session", credentials.sid)

  private val signInRequest =
    Post("https://calm.dhamma.org/en/users/sign_in", FormData(
      "user[login]" -> credentials.login,
      "user[password]" -> credentials.password,
      "commit" -> "Log In")).addHeader(expiredId)

  def signIn: Future[String@@SessionIdTag] = for {
    response <- Http().singleRequest(signInRequest)
    discard <- response.discardEntityBytes().future()
    session <- response.header[`Set-Cookie`].map(_.cookie.value)
      .fold(Future.failed[String](new Exception("Login failed".logError.logError(response))))(Future.successful)
  } yield session
}
