package calm.solid

import org.gbz.Tag.@@
import scala.concurrent.Future

trait WebCoreModule {
  this: WebEntityModel with CommonCoreModule =>

  def html[A:HtmlSource](t:A): Future[String @@ HtmlContent[A]] = HtmlSource[A].apply(t)
  trait HtmlSource[T] extends Apply[T,Future[String@@HtmlContent[T]]]
  object HtmlSource extends Instance[HtmlSource]

  def json[A:JsonSource](t:A): Future[String @@ JsonContent[A]] = JsonSource[A].apply(t)
  trait JsonSource[T] extends Apply[T,Future[String@@JsonContent[T]]]
  object JsonSource extends Instance[JsonSource]
}
