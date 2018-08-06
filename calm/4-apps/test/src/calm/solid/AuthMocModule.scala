package calm.solid

import calm.solid.AuthModule
import scala.concurrent.Future
import org.gbz.Tag._


trait AuthMocModule extends AuthModule {
  def mocAuthClient: AuthClient = new AuthClient {
    override def signIn: Future[SessionId] = Future.successful("<SessionId>".@@[SessionIdTag])
  }
}
