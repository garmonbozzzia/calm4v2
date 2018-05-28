package org.gbz.calm

/* Created on 24.05.18 */
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
    val C10d = Value("10-Day")
    val C3d = Value("3-DayOSC")
    val C1d = Value("1-DayOSC")
    val Sati = Value("Satipatthana")
  }

  object CourseVenues extends Enumeration {
    val DD = Value("Dhamma Dullabha")
    val Ekb = Value("Yekaterinburg")
    val Spb = Value("Saint Petersburg")
    val Nvsb = Value("Novosibirsk")
    val Msc = Value("Moscow")
    val Izhevsk = Value("Izhevsk")
  }
  type Gender = Genders.Value
  type Role = Roles.Value
  type ApplicantState = ApplicantStates.Value
  type CourseType = CourseTypes.Value
  type CourseVenue = CourseVenues.Value
}
