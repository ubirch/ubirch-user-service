package com.ubirch.user.model.db

import com.ubirch.util.date.DateUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime

/**
  *
  * @param id            unique user identifier
  * @param displayName
  * @param providerId
  * @param externalId    hashed mail address
  * @param locale
  * @param activeUser    if the user is active or not
  * @param email         email address
  * @param hashedEmail   sha256 hashed email
  * @param action        if user is to become de-/activated
  * @param executionDate when the user shall become de-/activated
  * @param created       date of creation
  * @param updated       date of last update
  */
case class User(id: String = UUIDUtil.uuidStr,
                displayName: String,
                providerId: String,
                externalId: String,
                locale: String,
                activeUser: Option[Boolean] = Some(false),
                email: Option[String] = None,
                hashedEmail: Option[String] = None,
                action: Option[Action] = None,
                executionDate: Option[DateTime] = None,
                created: DateTime = DateUtil.nowUTC,
                updated: DateTime = DateUtil.nowUTC
               )


sealed trait Action

case object Activate extends Action

case object Deactivate extends Action

object Action {

  def unsafeFromString(value: String): Action = value.toUpperCase match {
    case "ACTIVATE" => Activate
    case "DEACTIVATE" => Deactivate
  }

  def toFormattedString(status: Action): String = status match {
    case Activate => "ACTIVATE"
    case Deactivate => "DEACTIVATE"
  }

}
