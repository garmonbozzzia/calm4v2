package org.gbz.calm.model

import org.gbz.calm.CalmEnums.{CourseType, CourseTypes, CourseVenue, CourseVenues}

case class CourseList(courses: Seq[CourseRecord]){
  import CourseTypes._
  def cType(cTypes: CourseType*) = CourseList(courses.filter(x => cTypes.contains(x.cType)))
  def c10d: CourseList = cType(C10d)
  def c3d: CourseList = cType(C3d)
  def c1d: CourseList = cType(C1d)
  def sati: CourseList = cType(Sati)
  def all: CourseList = cType(C10d, C3d, C1d, Sati)
  def venue(vs: CourseVenue*) = CourseList(courses.filter(x => vs.contains(x.venue)))
  def dullabha: CourseList = venue(CourseVenues.DD)
  def ekb: CourseList = venue(CourseVenues.Ekb)
  def finished = CourseList(courses.filter(_.status == "Finished"))
  def scheduled = CourseList(courses.filter(_.status == "Scheduled"))
}
