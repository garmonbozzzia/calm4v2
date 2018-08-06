package calm

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import calm.Authentication._
import org.gbz.Global._
import org.gbz.utils.log.Log._
import wvlet.airframe.bind
import org.gbz.Tag._

import scala.concurrent.Future

trait AuthClientImpl extends AuthClient with LogSupport {

  val credentials = bind[Credentials]

  private val expiredId = Cookie("_sso_session", credentials.sid)

  private val signInRequest =
    Post("https://calm.dhamma.org/en/users/sign_in", FormData(
      "user[login]" -> credentials.login,
      "user[password]" -> credentials.password,
      "commit" -> "Log In")).addHeader(expiredId)

  def signIn: Future[SessionId] = for {
    response <- Http().singleRequest(signInRequest)
    discard <- response.discardEntityBytes().future()
    session <- response.header[`Set-Cookie`].map(_.cookie.value)
      .fold(Future.failed[String](new Exception("Login failed".logError.logError(response))))(Future.successful)
  } yield session
}
