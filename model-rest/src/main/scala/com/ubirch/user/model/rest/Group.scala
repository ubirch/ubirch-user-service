package com.ubirch.user.model.rest

import java.util.UUID

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class Group(id: Option[UUID],
                 displayName: String,
                 ownerId: UUID,
                 contextId: UUID,
                 allowedUsers: Seq[UUID]
                )
