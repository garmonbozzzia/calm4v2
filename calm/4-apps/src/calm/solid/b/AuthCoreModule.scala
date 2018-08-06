package calm.solid

import scala.concurrent.Future
import org.gbz.Global._
import org.gbz.ExtUtils._

trait AuthCoreModule extends AuthEntitiesModule {
  trait AuthStorage {
    def read(): Option[SessionId]
    def write(session: SessionId)
  }

  trait AuthClient {
    def signIn: Future[SessionId]
  }

  trait AuthManager {
    def sessionId: Future[SessionId]
  }

  implicit def authManager(implicit ac:AuthClient, as:AuthStorage): AuthManager = new AuthManager {
    override def sessionId: Future[SessionId] =
      as.read().fold(ac.signIn.map(_ <<< as.write))(Future(_))
  }
}