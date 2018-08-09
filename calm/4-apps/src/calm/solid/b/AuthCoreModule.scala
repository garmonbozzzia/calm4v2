package calm.solid

import org.gbz.utils.log.Log._
import scala.concurrent.Future

trait AuthCoreModule extends LogSupport {
  this: CommonCoreModule with AuthEntitiesModule =>
  def sessionId[T](implicit a: AuthManager): Future[SessionId] = a.value
  trait AuthManager extends Value[Future[SessionId]]
  object AuthManager {
    def pure(session: => Future[SessionId]): AuthManager = new AuthManager {
      def value: Future[SessionId] = session
    }
  }
}
