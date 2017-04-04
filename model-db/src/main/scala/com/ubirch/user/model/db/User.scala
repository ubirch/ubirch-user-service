package com.ubirch.user.model.db

import java.util.UUID

import com.ubirch.util.date.DateUtil
import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class User(id: UUID = UUIDUtil.uuid,
                displayName: String,
                providerId: String,
                externalId: String,
                created: DateTime = DateUtil.nowUTC,
                updated: DateTime = DateUtil.nowUTC
               )
