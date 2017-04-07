package com.ubirch.user.model.rest

import java.util.UUID

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class AllowedUsers(groupId: UUID,
                        allowedUsers: Set[UUID]
                       )
