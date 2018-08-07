package calm.solid

import org.gbz.Tag._
import org.gbz.Global._

import scala.concurrent.Future

object MockModules{
  trait MocAuthClient{
    this: AppModule =>
    override lazy val authClient: AuthManager@@NoStorage =
      AuthManager.pure(Future.successful("<SessionId>".@@[SessionIdTag])).@@[NoStorage]
  }

  trait MocHtmlSource{
    this: AppModule =>
    override def htmlSource[T: CalmUri](implicit auth: AuthManager @@ Default): HtmlSource[T] =
      x => Future(s"[HtmlContent: <$x>]")
  }

  trait MocJsonSource{
    this: AppModule =>
    override def jsonSource[T: CalmUri](implicit auth: AuthManager @@ Default): JsonSource[T] =
      x => Future(s"[JsonContent: <$x>]")
  }

  trait MocAuthStorage{
    this: AppModule =>
    override implicit lazy val authStorage: AuthStorage = ???
  }
}
