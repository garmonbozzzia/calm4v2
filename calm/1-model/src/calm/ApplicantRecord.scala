package calm

import wvlet.surface.tag._
import java.time.{LocalDate, LocalDateTime}

import CalmEnums._

import Types._
case class ApplicantRecord(
  aId: AppId,
  displayId: DisplayId,
  cId: CourseId,
  familyName: FamilyName,
  givenName: GivenName,
  gender: Gender,
  pregnant: Pregnancy,
  role: Role,
  email: Email,
  birthDate: BirthDate,
  age: Age,
  nSat: Int,
  nServe: Int,
  phoneHome: Phone,
  phoneMobile: Phone,
  receivedAt: LocalDateTime,
  enrolledAt: LocalDateTime,
  dismissedAt: LocalDateTime,
  state: ApplicantState
)
