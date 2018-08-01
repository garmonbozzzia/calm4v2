package calm

import wvlet.surface.tag._
import java.time.{LocalDate, LocalDateTime}

import CalmEnums._

case class ApplicantRecord(
  aId: Int @@ AppId,
  displayId: String @@ DisplayId,
  cId: Int @@ CourseId,
  familyName: String @@ FamilyName,
  givenName: String @@ GivenName,
  gender: Gender,
  pregnant: Boolean @@ Pregnancy,
  role: Role,
  email: String @@ Email,
  birthDate: LocalDate @@ BirthDate,
  age: Int @@ Age,
  nSat: Int,
  nServe: Int,
  phoneHome: String @@ Phone,
  phoneMobile: String @@ Phone,
  receivedAt: LocalDateTime,
  enrolledAt: LocalDateTime,
  dismissedAt: LocalDateTime,
  state: ApplicantState
)
