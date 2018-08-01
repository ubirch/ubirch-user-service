package com.ubirch.user.model.db

import com.ubirch.util.date.DateUtil
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.DateTime

/**
  *
  * @param id          unique user identifier
  * @param displayName
  * @param providerId
  * @param externalId
  * @param locale
  * @param activeUser
  * @param email       email address
  * @param hashedEmail sha256 hashed email
  * @param created
  * @param updated
  */
case class User(id: String = UUIDUtil.uuidStr,
                displayName: String,
                providerId: String,
                externalId: String,
                locale: String,
                activeUser: Option[Boolean] = Some(false),
                email: Option[String] = None,
                hashedEmail: Option[String] = None,
                created: DateTime = DateUtil.nowUTC,
                updated: DateTime = DateUtil.nowUTC
               )
