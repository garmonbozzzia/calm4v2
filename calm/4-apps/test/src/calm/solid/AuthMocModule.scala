package calm.solid

import calm.solid.AuthModule
import scala.concurrent.Future
import org.gbz.Tag._


trait AuthMocModule extends AuthModule {
  def mocAuthClient: AuthManager[NoStorage] =
    AuthManager.pure[NoStorage](Future.successful("<SessionId>".@@[SessionIdTag]))
}
