package calm

import java.time.{LocalDate, LocalDateTime}

import utest._

/* Created on 31.07.18 */
object Sandbox extends TestSuite{
  override def tests = Tests{
    * - {
      import wvlet.surface.tag._
      import CalmEnums._

      val age: Int @@ AppId = 123.taggedWith[AppId]
      ApplicantRecord(0.taggedWith[AppId],"displayId", age, "fn", "gn", Genders.Female, false, Roles.NewStudent,"w@w.tu", LocalDate.now(),
        50, 0, 0, "+89998","", LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), ApplicantStates.Arrived)
    }
  }
}
