package com.ubirch.user.core.manager.util

import com.ubirch.user.model.db.Group
import com.ubirch.user.model.rest.UserInfoGroup
import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-04-25
  */
object UserInfoUtil {

  def toUserInfoGroups(groups: Set[Group]): Set[UserInfoGroup] = {

    // TODO automated tests
    groups map { group =>
      UserInfoGroup(
        id = UUIDUtil.fromString(group.id),
        displayName = group.displayName,
        adminGroup = group.adminGroup
      )
    }

  }

}

