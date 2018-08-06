package calm.solid

import scala.concurrent.Future
import org.gbz.Global._
import org.gbz.ExtUtils._
import org.gbz.Tag.@@
import org.gbz.utils.log.Log._

trait AuthCoreModule extends LogSupport {
  this: CoreModule with AuthEntitiesModule =>

  def sessionId[T](implicit a:AuthManager@@Default): Future[SessionId] = a.sessionId

  def withStorage(ac:AuthManager)(implicit as:AuthStorage): AuthManager =
    AuthManager.pure(as.read().fold(ac.sessionId.map(_ <<< as.write))(Future(_)))

  trait AuthStorage {
    def read(): Option[SessionId]
    def write(session: SessionId)
  }

  trait AuthManager {
    def sessionId: Future[SessionId]
  }

  object AuthManager{
    def pure(session: => Future[SessionId]): AuthManager = new AuthManager {
      def sessionId: Future[SessionId] = session
    }
  }
}
