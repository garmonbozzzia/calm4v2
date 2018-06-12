package org.gbz.calm.model

import org.gbz.calm.CalmEnums.{ApplicantState, Gender, Role}

case class MergedApplicantRecord(
                                  cId: String,
                                  aId: Int,
                                  displayId: String,
                                  givenName: String,
                                  familyName: String,
                                  age: Int,
                                  gender: Gender,
                                  role: Role,
                                  pregnant: Boolean,
                                  nSat: Int,
                                  nServe: Int,
                                  state: ApplicantState
                                )
