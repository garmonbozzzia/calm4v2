package org.gbz

import ammonite.ops._

object ExtUtils {

  object Timer {
    def apply[T,R](expr: => T)(tf: Long => Any)(cont: T => R) = {
      val start = System.currentTimeMillis
      val res = expr
      tf(System.currentTimeMillis - start)
      cont(res)
    }
  }

  implicit class ZipExt[A](val obj: Iterable[A]) extends AnyVal {
    def zipWith[B,C](that: Iterable[B])(f: (A, B) => C): Iterable[C] = obj.zip(that).map(_.reduce(f))
    //def zip[A1 >: A, B, That](that: GenIterable[B])(implicit bf: CanBuildFrom[Repr, (A1, B), That]): That = {
  }

  implicit class Tuple2Ext[A,B](val obj: Tuple2[A,B]) extends AnyVal {
    def reduce[C](f: (A,B) => C) = f(obj._1, obj._2)
  }

  implicit class TransformImplicit[T](val obj: T) extends AnyVal {
    def iapl[B](f: T => B): T = {f(obj); obj}
    def rapl[B](f: T => B): B = f(obj)
    def <|[B](f: T => B): T = {f(obj); obj}
    def <*[B](expr: => B): T = {expr; obj}
    def |?>(condition: Boolean)(transform: T => T): T =
      if(condition) transform(obj) else obj
    def |?>(condition: T => Boolean)(transform: T => T): T =
      if(condition(obj)) transform(obj) else obj
    def |?!>(condition: Boolean)(transform: T => T): T =
      if(!condition) transform(obj) else obj
    def |?!>(condition: T => Boolean)(transform: T => T): T =
      if(!condition(obj)) transform(obj) else obj
  }

  def assertEq[T](y: T): T => Unit = x => assert(x == y)
  implicit class BackArrowAssert[T](val left: T) extends AnyVal {
    //def <== (right: Any) = {assert(left == right); left}
    def <== (right: Any): T = left <| assertEq(right)
  }

  implicit class Mapable (val cc: Any) extends AnyVal {
    def ccToMap: Map[String, Any] =
      (Map[String, Any]() /: cc.getClass.getDeclaredFields) {
        (a, f) =>
          f.setAccessible(true)
          a + (f.getName -> f.get(cc))
      }
  }
}
