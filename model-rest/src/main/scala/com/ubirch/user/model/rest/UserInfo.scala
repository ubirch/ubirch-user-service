package com.ubirch.user.model.rest

import java.util.UUID

/**
  * author: cvandrei
  * since: 2017-04-20
  */
case class UserInfo(displayName: String,
                    locale: String,
                    myGroups: Set[UserInfoGroup] = Set.empty,
                    allowedGroups: Set[UserInfoGroup] = Set.empty,
                    activeUser: Boolean = false
                   )

case class UserInfoGroup(id: UUID,
                         displayName: String,
                         adminGroup: Option[Boolean] = None
                        )
