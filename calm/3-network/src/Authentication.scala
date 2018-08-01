package calm

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}
import org.gbz.ExtUtils._
import wvlet.airframe._
import Global._
import org.gbz.utils.log.Log.LogSupport
import wvlet.surface.tag._
import scala.concurrent.Future
import org.gbz.utils.log.Log._


// 1 - Model

trait Login
trait Password
trait SessionId
object SessionId
case class Credentials(login: String@@Login, password: String@@Password, sid: String@@SessionId)

//2-core
object Core {
  def defaultCredentials: Credentials = {
    val Seq(login, password, oldSid) = scala.io.Source.fromFile("data/login2").getLines().toSeq
    Credentials(login, password, oldSid)
  }
}

trait Storage[T] {
  def write(obj: T): Unit
  def read[U](key: U): Option[T]
}
trait SessionStorage extends Storage[String@@SessionId]

trait AuthClient {
  def signIn: Future[String@@SessionId]
}
trait AuthClientBind{ val authClient = bind[AuthClient]}

class AuthManager {
  private val storage = bind[SessionStorage]
  def sessionId: Future[String @@ SessionId] =
    storage.read(SessionId).fold(bind[AuthClient].signIn.map(_.iapl(storage.write)))(Future(_))
}


//3-impl


class InMemoStorage extends SessionStorage {
  var storage = Map.empty[String, String@@SessionId]
  def key[T,K](obj: T): String = "sId"
  override def write(obj: String@@SessionId) = storage = storage.updated(key(obj),obj)
  override def read[T](request: T): Option[String@@SessionId] = storage.get(key(request))
}


trait AuthClientImpl extends AuthClient with LogSupport {

  val credentials = bind[Credentials]

  private val expiredId = Cookie("_sso_session", credentials.sid)

  private val signInRequest =
    Post("https://calm.dhamma.org/en/users/sign_in", FormData(
      "user[login]" -> credentials.login,
      "user[password]" -> credentials.password,
      "commit" -> "Log In")).addHeader(expiredId)

  def signIn: Future[String@@SessionId] = for {
    response <- Http().singleRequest(signInRequest)
    discard = response.discardEntityBytes()
    _ <- discard.future()
    _ = discard.completionStage()
    session <- response.header[`Set-Cookie`].map(_.cookie.value)
      .fold(Future.failed[String](new Exception("Login failed".logError)))(Future.successful)
  } yield session//.log
}

//4-app

object Main extends App with LogSupport {
  val manager = newDesign
    .bind[SessionStorage].to[InMemoStorage]
    .bind[Credentials].toInstance(Core.defaultCredentials)
    .bind[AuthClient].to[AuthClientImpl]
    .newSession.build[AuthManager]
  manager.sessionId.log
    .flatMap(x => manager.log(x).sessionId.map(_.log))
    .onComplete(x => system.terminate().log(x))
}

object Test extends App with LogSupport {
  class MocAuthClient extends AuthClient {
    override def signIn: Future[String @@ SessionId] = Future(System.currentTimeMillis().toString.taggedWith[SessionId])
  }
  val manager = newDesign
    .bind[SessionStorage].to[InMemoStorage]
    .bind[Credentials].toInstance(Credentials("login", System.currentTimeMillis().toString, "AAA"))
    .bind[AuthClient].to[MocAuthClient].newSession.build[AuthManager]
  manager.sessionId.log
    .flatMap(x => manager.log(x).sessionId.map(_.log))
    .onComplete(x => system.terminate().log(x))
}