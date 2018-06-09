package org.gbz.calm.model

import org.gbz.calm.CalmEnums.{CourseType, CourseTypes, CourseVenue, CourseVenues}

case class CourseList(courses: Seq[CourseRecord]){
  import CourseTypes._
  def cType(cTypes: CourseType*) = CourseList(courses.filter(x => cTypes.contains(x.cType)))
  def c10d = cType(C10d)
  def c3d = cType(C3d)
  def c1d = cType(C1d)
  def sati = cType(Sati)
  def all = cType(C10d, C3d, C1d, Sati)
  def venue(vs: CourseVenue*) = CourseList(courses.filter(x => vs.contains(x.venue)))
  def dullabha = venue(CourseVenues.DD)
  def ekb = venue(CourseVenues.Ekb)
  def finished = CourseList(courses.filter(_.status == "Finished"))
  def scheduled = CourseList(courses.filter(_.status == "Scheduled"))
}
