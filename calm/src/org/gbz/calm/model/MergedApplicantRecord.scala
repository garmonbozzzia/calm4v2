package org.gbz.calm.model

import org.gbz.calm.CalmEnums._

import scala.util.Try

case class MergedApplicantRecord(
                                  aId: AppId,
                                  displayId: DisplayId,
                                  cId: CourseId,
                                  familyName: String,
                                  givenName: String,
                                  gender: Gender,
                                  pregnant: Boolean,
                                  role: Role,
                                  email: String,
                                  birthDate: String,
                                  age: Int,
                                  nSat: Int,
                                  nServe: Int,
                                  phoneHome: String,
                                  phoneMobile: String,
                                  receivedAt: String,
                                  enrolledAt: String,
                                  dismissedAt: String,
                                  state: ApplicantState
                                )

import ammonite.ops.Extensions._
object MergedApplicantRecord {
  def apply(data: Map[String,String]): Option[MergedApplicantRecord] = Try(new MergedApplicantRecord(
      aId = data("aId").toInt,
      displayId = data("displayId"),
      cId = data("cId").toInt,
      familyName = data("familyName"),
      givenName = data("givenName"),
      gender = data("gender") |> Genders.withName,
      pregnant = data("pregnant").toBoolean,
      role = data("role") |> Roles.withName,
      email = data("email"),
      birthDate = data("birthDate"),
      age = data("age").toInt,
      nSat = data("nSat").toInt,
      nServe = data("nServe").toInt,
      phoneHome = data("phoneHome"),
      phoneMobile = data("phoneMobile"),
      receivedAt = data("receivedAt"),
      enrolledAt = data("enrolledAt"),
      dismissedAt = data("dismissedAt"),
      state = data("state") |> ApplicantStates.withName
    )
  ).toOption
  def apply(jsonData: ApplicantRecord, htmlData: ApplicantHtmlRecord): MergedApplicantRecord =
    new MergedApplicantRecord(
      aId = jsonData.aId,
      displayId = jsonData.displayId,
      cId = jsonData.cId,
      familyName = jsonData.familyName,
      givenName = jsonData.givenName,
      gender = jsonData.gender,
      pregnant = jsonData.pregnant,
      role = jsonData.role,
      email = htmlData.email,
      birthDate = htmlData.birthDate,
      age = jsonData.age,
      nSat = jsonData.nSat,
      nServe = jsonData.nServe,
      phoneHome = htmlData.phoneHome,
      phoneMobile = htmlData.phoneMobile,
      receivedAt = htmlData.receivedAt,
      enrolledAt = htmlData.enrolledAt,
      dismissedAt = htmlData.dismissedAt,
      state = jsonData.state
    )
}