package com.ubirch.user.model.rest

import org.joda.time.DateTime

case class ActivationUpdate(updates: Seq[UserActivationUpdate])

object ActivationUpdate {
  val header = "externalId;targetStatus;executionDate;previousExecutionDate"
  val errorHeader = "externalId;targetStatus;executionDate;errorMessage"
  val lineBreak = "\n"
  val extIdNotExisting = "externalId not existing."
  val dateInPast = "executionDate being in the past."
  def targetStateWrong(active: Boolean) = s"target status of activeUser flag already being '$active'."

}

case class UserActivationUpdate(externalId: String, activate: Boolean, executionDate: Option[DateTime] = None) {

  def toErrorCsv = s"$externalId;$activate;${executionDate.getOrElse("")};update not possible due to "

  def toCSV(previousDate: Option[DateTime]) =
    s"$externalId;$activate;${executionDate.getOrElse("")};${previousDate.getOrElse("")}"
}
