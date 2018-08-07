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
  implicit def toTaggedTypeA[A, Tag]: A => A @@ Tag = _.taggedWith[Tag]

  //todo why not works
  //      Option[Int@@String] = Some(1)

  implicit def toTaggedPair[A, B, TagA, TagB](obj: (A,B))(
    implicit toTTA: A => A@@TagA, toTTB: B => B@@TagB): (A @@ TagA, B @@ TagB) =
    (toTTA(obj._1), toTTB(obj._2))
  implicit def toTaggedOption[A, Tag](obj: Option[A])(implicit toTT: A => A @@ Tag): Option[A @@ Tag] = obj.map(toTT)
}
