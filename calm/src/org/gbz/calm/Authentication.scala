package org.gbz.calm

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Post
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.headers.{Cookie, `Set-Cookie`}

import scala.concurrent.{Future, Promise}
import org.gbz.utils.log.Log._
import org.gbz.ExtUtils._
import Global._

object Authentication {

  private val Seq(login, password) = scala.io.Source.fromFile("data/login").getLines().toSeq
  private val expiredId = Cookie("_sso_session", "am1La2dFQ3F2QjRtVUxQUlNEMmtRbW1SSitjRlJVTndFZ0Q5YmYwQlB3Y29pVExDTitNTEQ0M2lOOTB3eUFWSlFtTHpBMU9ZRGM1WXlUSTVmbGR6LzBaaVZDYkgzZjZJYm9RU0VGRVkyMjg0bU5IMGhGSkQ3REJJRE51N09YTWJTVkYzSlZxdGFQc05aOVNNZ1BNSm8yamVhMG83Zys5alZXU1pxWlZJalhxSVpaQ01DRm1WcnoxUGZCN3J4YjlqY2t4TlNHajhPenhmd0RqdkhBM2gzSUxaUUU4dU5QaWFsY1dUSTB5TDNCUUltNVFJait0YXpXUGFEMHFDdVpaZC9mK0NkZTNXWGxqZU4wTkZDTzJSMVgvNGFHaXVWcGVlTWVNcmFEQkZxSlE0ZldCdDBuVm9ZbHcyaW5QV3pZemM3cGppSTduRUJrbDBXTzVLdlhJVGUxSWFJc3VmN3h2L2Urd2QrZkZxTysvbTl1WTlzOUJsREx0QkpBVXFPUjlRdURZRFVCQ01vdjNvZmgzaGVBWG9RVFFzeGcrZTI1a1gzRHpKWGY0Vmloc0hhdmRISWt5UytwMnRFK09QMzYwNDBZM0ZiS3YyZytuVURqQnJqU0VNQlRqbHAyaDFCYVlTK1p3ZzNlenJvTUE9LS1xeTVYTzNLdkRyN1hwVVdvdFlLYWZRPT0%3D--81de5e26e59ee0e8c8534da5374e5a972d91278a")
  private val signInRequest =
    Post("https://calm.dhamma.org/en/users/sign_in", FormData(
      "user[login]" -> login,
      "user[password]" -> password,
      "commit" -> "Log In")).addHeader(expiredId)

  private def signIn: Future[String] = for {
      response <- Http().singleRequest(signInRequest)
      discard = response.discardEntityBytes()
      _ <- discard.future()
      _ = discard.completionStage().trace
      session <- response.header[`Set-Cookie`].map(_.cookie.value)
        .fold(Future.failed[String](new Exception("Login failed").trace))(Future.successful)
    } yield session.trace

  def newCookie: Future[Cookie] = signIn.map(Cookie("_sso_session", _))
  def sessionKey = "_sessionId"
  def save(sId: String) = sId.<|(CalmDb.redisClient.setex(sessionKey, 1750, _))
  lazy val storage = Mock.mock[Storage[String]](SessionStorage.redis)(SessionStorage.inMemory)
  def cookie = storage.get.fold(signIn.map(_ <| storage.save))(Promise.successful(_).future)
    .map(Cookie("_sso_session", _))
}

trait Storage[T] {
  def save(id: T): Unit
  def get: Option[T]
}

object SessionStorage {

  def redis = new Storage[String] {
    def sessionKey = "_sessionId"
    override def save(id: String): Unit = CalmDb.redisClient.setex(sessionKey, 1750, id)
    override def get = CalmDb.redisClient.get(sessionKey)
  }

  def inMemory = new Storage[String] {
    var sId: Option[String] = None
    override def save(id: String): Unit = sId = Some(id)
    override def get = sId
  }
}
