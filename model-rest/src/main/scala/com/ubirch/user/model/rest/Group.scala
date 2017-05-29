package com.ubirch.user.model.rest

import java.util.UUID

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class Group(id: Option[UUID] = None,
                 displayName: String,
                 ownerId: UUID,
                 contextId: UUID,
                 allowedUsers: Set[UUID] = Set.empty,
                 adminGroup: Option[Boolean] = None
                )
