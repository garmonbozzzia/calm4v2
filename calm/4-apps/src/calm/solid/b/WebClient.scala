package calm.solid

import org.gbz.Tag.@@

import scala.concurrent.Future

trait WebClient[A[_], B[_]] {
  def get[T:A,R:B](request: T): Future[String@@R]
  def html[T:A](request: T)(implicit b: B[HtmlSource[T]]): Future[String@@HtmlSource[T]] = get(request)
  def json[T:A](request: T)(implicit b: B[JsonSource[T]]): Future[String@@JsonSource[T]] = get(request)
}