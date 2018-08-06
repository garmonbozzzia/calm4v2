package calm.solid

import scala.concurrent.Future
import org.gbz.Global._
import org.gbz.ExtUtils._
import org.gbz.utils.log.Log._

trait AuthCoreModule extends AuthEntitiesModule with LogSupport {
  trait AuthStorage {
    def read(): Option[SessionId]
    def write(session: SessionId)
  }

  trait NoStorage
  trait AuthManager[T] {
    def sessionId: Future[SessionId]
  }
  object AuthManager{
    def apply[A](implicit v: AuthManager[A] ): AuthManager[A] = v
    def pure[A](session: => Future[SessionId]): AuthManager[A] = new AuthManager[A] {
      def sessionId: Future[SessionId] = session
    }
  }

  implicit def authManager[A](implicit ac:AuthManager[NoStorage], as:AuthStorage): AuthManager[A] =
    AuthManager.pure[A](as.read().fold(ac.sessionId.map(_ <<< as.write))(Future(_))).log("Invoked")
}