package calm

import java.time.LocalDate

import org.gbz.Tag._

//trait AppId
//trait DisplayId
//trait CourseId
//trait FamilyName
//trait GivenName
//trait Pregnancy
//trait Email
//trait BirthDate
//trait Age
//trait Phone

object Types {
  trait AppIdTag
  trait DisplayIdTag
  trait CourseIdTag
  trait FamilyNameTag
  trait GivenNameTag
  trait PregnancyTag
  trait EmailTag
  trait BirthDateTag
  trait AgeTag
  trait PhoneTag
  type AppId      = Int @@ AppIdTag
  type DisplayId  = String @@ DisplayIdTag
  type CourseId   = Int @@ CourseIdTag
  type FamilyName = String @@ FamilyNameTag
  type GivenName  = String @@ GivenNameTag
  type Pregnancy  = Boolean @@ PregnancyTag
  type Email      = String @@ EmailTag
  type BirthDate  = LocalDate @@ BirthDateTag
  type Age        = Int @@ AgeTag
  type Phone      = String @@ PhoneTag
}

object CalmEnums {

  object Genders extends Enumeration {
    val Male = Value("M")
    val Female = Value("F")
  }

  object Roles extends Enumeration {
    val NewStudent = Value("N")
    val OldStudent = Value("O")
    val Server = Value("S")
  }

  object ApplicantStates extends Enumeration {
    val NewPendingForConfirmation = Value("NewPendingForConfirmation")
    val PendingForConfirmation = Value("PendingForConfirmation")
    val Confirmed = Value("Confirmed")
    val RequestedReconfirm = Value("RequestedReconfirm")
    val Reconfirmed = Value("Reconfirmed")
    val Arrived = Value("Arrived")
    val Left = Value("Left")
    val Completed = Value("Completed")
    val NewPendingForWaitlist = Value("NewPendingForWaitlist")
    val PendingForWaitlist = Value("PendingForWaitlist")
    val ConfirmableWaitlist = Value("ConfirmableWaitlist")
    val WaitListReqReconfirm = Value("WaitListReqReconfirm")
    val WaitListReconfirmed = Value("WaitListReconfirmed")
    val NewApplication = Value("NewApplication")
    val NewNoVacancy = Value("NewNoVacancy")
    val NoVacancy = Value("NoVacancy")
    val NoShow = Value("NoShow")
    val Cancelled = Value("Cancelled")
    val Discontinued = Value("Discontinued")
    val Refused = Value("Refused")
  }

  object CourseTypes extends Enumeration {
    val ServicePeriod = Value("ServicePeriod")
    val C10d = Value("10-Day")
    val C3d = Value("3-DayOSC")
    val C1d = Value("1-DayOSC")
    val Sati = Value("Satipatthana")
    val Child = Value("Child")
    val Teen = Value("Teen")
    val C20d = Value("20-Day")
    val C30d = Value("30-Day")
  }

  object CourseVenues extends Enumeration {
    val DD = Value("Dhamma Dullabha")
    val Ekb = Value("Yekaterinburg")
    val Spb = Value("Saint Petersburg")
    val Nvsb = Value("Novosibirsk")
    val Msc = Value("Moscow")
    val Izhevsk = Value("Izhevsk")
    val Perm = Value("Perm")
  }

  object CourseStatuses extends Enumeration {
    val Finished = Value("Finished")
    val InProgress = Value("In Progress")
    val NotOpened = Value("Not opened")
    val Scheduled = Value("Scheduled")
    val Tentative = Value("Tentative")
  }


  type Gender = Genders.Value
  type Role = Roles.Value
  type ApplicantState = ApplicantStates.Value
  type CourseType = CourseTypes.Value
  type CourseVenue = CourseVenues.Value
  type CourseStatus =CourseStatuses.Value
}
