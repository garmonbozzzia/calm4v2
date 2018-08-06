package org.gbz

import scala.language.implicitConversions
import scala.language.postfixOps

object Tag {
  type Tag[+U]       = { type Tag <: U }
  type @@[T, +U]     = T with Tag[U]
  type Tagged[T, +U] = T with Tag[U]
  implicit class Tagger[T](t: T) {
    def taggedWith[U]: T @@ U = t.asInstanceOf[T @@ U]
    def @@[U]: T @@ U = t.asInstanceOf[T @@ U]
  }

  implicit def toTaggedType[A, Tag](obj: A): A @@ Tag = obj.taggedWith[Tag]
  implicit def toTaggedOption[A, Tag](obj: Option[A])(implicit toTT: A => A @@ Tag): Option[A @@ Tag] = obj.map(toTT)
}
