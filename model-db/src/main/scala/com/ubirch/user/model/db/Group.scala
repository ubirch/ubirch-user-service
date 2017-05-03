package com.ubirch.user.model.db

import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class Group(id: String = UUIDUtil.uuidStr,
                 displayName: String,
                 ownerId: String,
                 contextId: String,
                 allowedUsers: Set[String],
                 created: DateTime = DateTime.now,
                 updated: DateTime = DateTime.now
                )
