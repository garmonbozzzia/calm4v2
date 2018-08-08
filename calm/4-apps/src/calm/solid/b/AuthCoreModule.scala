package calm.solid

import scala.concurrent.Future
import org.gbz.Global._
import org.gbz.ExtUtils._
import org.gbz.Tag.@@
import org.gbz.utils.log.Log._

trait AuthCoreModule extends LogSupport {
  this: CommonCoreModule with AuthEntitiesModule =>

  def sessionId[T](implicit a:AuthManager): Future[SessionId] = a.value

  def withStorage(ac:AuthManager)(implicit as:AuthStorage): AuthManager =
    AuthManager.pure(as.read().fold(ac.value.map(_ <<< as.write))(Future(_)))

  trait AuthStorage {
    def read(): Option[SessionId]
    def write(session: SessionId): Unit
  }

  trait AuthManager extends Value[Future[SessionId]]

  object AuthManager{
    def pure(session: => Future[SessionId]): AuthManager = new AuthManager {
      def value: Future[SessionId] = session
    }
  }
}
