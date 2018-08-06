package org.gbz.calm.model

import org.gbz.calm.CalmEnums.{ApplicantState, ApplicantStates, Gender, Role}
import org.gbz.calm.CalmEnums.ApplicantStates.{Cancelled, Completed, Left}
import org.gbz.calm.CalmEnums.Genders.{Female, Male}
import org.gbz.calm.CalmEnums.Roles.{NewStudent, OldStudent, Server}

import scala.collection.immutable

object AppList {
  trait Extractor[T] { def extractor: MergedApplicantRecord => T}

  implicit val roleExt: Extractor[Role] = Extractor.pure(_.role)
  implicit val genderExt: Extractor[Gender] = Extractor.pure(_.gender)
  implicit val stateExt: Extractor[ApplicantState] = Extractor.pure(_.state)

  object Extractor {
    def apply[T](implicit extractor: Extractor[T]) = extractor
    def pure[T](f: MergedApplicantRecord => T) = new Extractor[T] {def extractor = f}
  }
}

case class AppList(apps: Seq[MergedApplicantRecord]) {
  import AppList._

  def filter[T](st: T*)(implicit ext: Extractor[T]): AppList =
    AppList(apps.filter(x => st.contains(ext.extractor(x))))
  implicit def t2f[T](st: T)(implicit ext: Extractor[T]): AppList = filter(st)

  def n: AppList = NewStudent
  def o: AppList = OldStudent
  def s: AppList = Server
  def m: AppList = Male
  def f: AppList = Female
  def complete: AppList = Completed
  def left: AppList = Left
  def cancelled: AppList = Cancelled

  def states: List[(ApplicantState, Int)] = ApplicantStates.values
    .map(x => x -> apps.count(_.state == x))
    .filter(_._2 > 0).toList.sortBy(-_._2)
  def ages: immutable.IndexedSeq[(Int, Int)] = (16 to 80).map(age => age -> apps.count(_.age == age))
}