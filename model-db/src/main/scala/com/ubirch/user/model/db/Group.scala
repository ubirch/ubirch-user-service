package com.ubirch.user.model.db

import java.util.UUID

import com.ubirch.util.uuid.UUIDUtil

import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class Group(id: UUID = UUIDUtil.uuid,
                 displayName: String,
                 ownerId: UUID,
                 contextId: UUID,
                 allowedUsers: Set[UUID],
                 created: DateTime = DateTime.now,
                 updated: DateTime = DateTime.now
                )
