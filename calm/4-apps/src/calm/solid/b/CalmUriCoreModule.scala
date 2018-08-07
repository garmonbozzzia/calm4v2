package calm.solid

import akka.http.scaladsl.model.Uri
import org.gbz.Tag._

trait CalmUriCoreModule {
  this: CalmEntitiesModule with CommonCoreModule =>
  def uri[A:CalmUri](a:A): Uri@@UriTag[A] = CalmUri[A](a)

  trait CalmUri[T] extends Apply[CalmUri,T,Uri]

  object CalmUri{
    def apply[T](t:T)(implicit v:CalmUri[T]) = v(t)
  }
}
