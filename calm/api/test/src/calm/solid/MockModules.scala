package calm.solid

import org.gbz.Tag._
import org.gbz.Global._
import org.gbz.utils.log.Log._

import scala.concurrent.Future

object MockModules {

  trait MocAuthClient {
    this: AppModule =>
    override lazy val authClient: AuthManager =
      AuthManager.pure(Future.successful("<SessionId>".@@[SessionIdTag]))
  }

  trait MocHtmlSource {
    this: AppModule =>
    override def htmlSource[T: CalmUri](
      implicit auth: AuthManager
    ): HtmlSource[T] =
      x => Future(s"[HtmlContent: <$x>]")
  }

  trait MocJsonSource {
    this: AppModule =>
    override def jsonSource[T: CalmUri](
      implicit auth: AuthManager
    ): JsonSource[T] =
      x => Future(s"[JsonContent: <$x>]")
  }

  trait MocAuthStorage {
    this: AppModule with LogSupport =>
    var storedId: Option[SessionId] = None
    override implicit lazy val sessionReader: ReaderSingle[SessionId] =
      ReaderSingle.pure(storedId.logWith(s => s"! - Read: $s"))
    override implicit lazy val sessionWriter: Writer[SessionId] =
      s => storedId = Some(s).logWith(s => s"! - Wrote: $s")
  }

}
