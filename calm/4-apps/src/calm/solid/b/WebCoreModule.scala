package calm.solid

import org.gbz.Tag.@@
import scala.concurrent.Future

trait WebCoreModule extends WebEntityModel with AuthCoreModule{

  def html[A:HtmlSource](t:A): Future[String @@ HtmlContent[A]] = HtmlSource[A](t)
  def json[A:JsonSource](t:A): Future[String @@ JsonContent[A]] = JsonSource[A](t)

  trait HtmlSource[T] extends Apply[HtmlSource,T,Future[String@@HtmlContent[T]]]
  trait JsonSource[T] extends Apply[JsonSource,T,Future[String@@JsonContent[T]]]

  object JsonSource {
    def apply[A](a:A)(implicit v: JsonSource[A]): Future[String @@ JsonContent[A]] = v(a)
  }

  object HtmlSource {
    def apply[A](a:A)(implicit v: HtmlSource[A]): Future[String @@ HtmlContent[A]] = v(a)
  }
}
