package org.gbz

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

  implicit def toTaggedPair[A, B, TagA, TagB](obj: (A,B))(
    implicit toTTA: A => A@@TagA, toTTB: B => B@@TagB): (A @@ TagA, B @@ TagB) =
    (toTTA(obj._1), toTTB(obj._2))
  implicit def toTaggedOption[A, Tag](obj: Option[A])(implicit toTT: A => A @@ Tag): Option[A @@ Tag] = toTaggedOptionB[A](obj).apply

  def toTaggedOptionB[A](a:Option[A]) = new {
    def apply[Tag](implicit toTT: A => A @@ Tag): Option[A @@ Tag] = a.map(toTT)
  }

}
