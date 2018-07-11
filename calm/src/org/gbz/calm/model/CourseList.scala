package org.gbz.calm.model

import org.gbz.calm.CalmEnums._

object CourseList{
  trait Extractor[T] { def extractor: CourseRecord => T}

  implicit val typeExt: Extractor[CourseType] = Extractor.pure(_.cType)
  implicit val statusExt: Extractor[CourseStatus] = Extractor.pure(_.status)
  implicit val venueExt: Extractor[CourseVenue] = Extractor.pure(_.venue)

  object Extractor {
    def apply[T](implicit extractor: Extractor[T]) = extractor
    def pure[T](f: CourseRecord => T) = new Extractor[T] {def extractor = f}
  }
}

case class CourseList(courses: Seq[CourseRecord]){
  import CourseList._
  import CourseTypes._
  import CourseStatuses._
  import CourseVenues._
  def filter[T](st: T*)(implicit ext: Extractor[T]): CourseList =
    CourseList(courses.filter(x => st.contains(ext.extractor(x))))
  implicit def t2f[T](t: T)(implicit ext: Extractor[T]): CourseList = filter(t)
  def c10d: CourseList = C10d
  def c3d: CourseList = C3d
  def c1d: CourseList = C1d
  def sati: CourseList = Sati
  def all: CourseList = filter(C10d, C3d, C1d, Sati)
  def dullabha: CourseList = DD
  def ekb: CourseList = Ekb
  def finished: CourseList = Finished
  def scheduled: CourseList = NotOpened
  def inProgress: CourseList = InProgress
}
