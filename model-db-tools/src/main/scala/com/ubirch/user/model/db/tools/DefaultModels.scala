package com.ubirch.user.model.db.tools

import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-04-06
  */
object DefaultModels {

  def context(displayName: String = "automated-test"): Context = {

    Context(
      displayName = displayName
    )

  }

  def user(displayName: String = "automated-test-user",
           providerId: String = "google",
           externalId: String = UUIDUtil.uuidStr,
           locale: String = "en"
          ): User = {

    User(
      displayName = displayName,
      providerId = providerId,
      externalId = externalId,
      locale = locale
    )

  }

  def group(displayName: String = s"group-${UUIDUtil.uuid}",
            ownerId: String = UUIDUtil.uuidStr,
            contextId: String = UUIDUtil.uuidStr,
            allowedUsers: Set[String] = Set.empty
           ): Group = {

    Group(
      displayName = displayName,
      ownerId = ownerId,
      contextId = contextId,
      allowedUsers = allowedUsers
    )

  }

}
