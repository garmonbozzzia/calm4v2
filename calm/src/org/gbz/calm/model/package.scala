package org.gbz.calm

import java.text.SimpleDateFormat
import java.util.Date

package object model {
  type CourseId = Int
  type DisplayId = String
  type AppId = Int

  val dateFormat = new SimpleDateFormat("yyyy-mm-dd")

  class CourseDate(date: Date){
    override def toString: DisplayId = dateFormat.format(date)
  }
  object CourseDate {
    val timezoneDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
    def apply(s: String) = new CourseDate(dateFormat.parse(s))
  }
}
