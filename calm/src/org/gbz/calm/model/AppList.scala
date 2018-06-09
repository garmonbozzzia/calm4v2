package org.gbz.calm.model

import org.gbz.calm.CalmEnums.{ApplicantState, ApplicantStates}
import org.gbz.calm.CalmEnums.ApplicantStates.{Cancelled, Completed, Left}
import org.gbz.calm.CalmEnums.Genders.{Female, Male}
import org.gbz.calm.CalmEnums.Roles.{NewStudent, OldStudent, Server}

import scala.collection.immutable

case class AppList(apps: Seq[ApplicantRecord]) {
  def filterT[V](extractor: ApplicantRecord => V)(st: V*) =
    AppList(apps.filter(x => st.contains(extractor(x))))

  //    def n = AppList(apps.filter(_.role == NewStudent))
  def n = filterT(_.role)(NewStudent)
  def o = filterT(_.role)(OldStudent)
  def s = filterT(_.role)(Server)
  def m = filterT(_.gender)(Male)
  def f = filterT(_.gender)(Female)
  def complete: AppList = filterT(_.state)(Completed)
  def left: AppList = filterT(_.state)(Left)
  def cancelled: AppList = filterT(_.state)(Cancelled)

  def states: List[(ApplicantState, Int)] = ApplicantStates.values
    .map(x => x -> apps.count(_.state == x))
    .filter(_._2 > 0).toList.sortBy(-_._2)
  def ages: immutable.IndexedSeq[(Int, Int)] = (16 to 80).map(age => age -> apps.count(_.age == age))
}