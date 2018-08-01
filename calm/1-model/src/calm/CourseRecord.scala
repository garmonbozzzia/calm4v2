package calm
import java.time.{LocalDate}

import CalmEnums._
case class CourseRecord(cId: CourseId,
                        start: LocalDate,
                        end: LocalDate,
                        cType: CourseType,
                        venue: CourseVenue,
                        status: CourseStatus)
