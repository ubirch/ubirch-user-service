package com.ubirch.user.model.db.tools

import java.util.UUID

import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.util.uuid.UUIDUtil

import scala.util.Random

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
           externalId: String = Random.nextInt().toString
          ): User = {

    User(
      displayName = displayName,
      providerId = providerId,
      externalId = externalId
    )

  }

  def group(displayName: String = s"group-${Random.nextInt}",
            ownerId: UUID = UUIDUtil.uuid,
            contextId: UUID = UUIDUtil.uuid,
            allowedUsers: Set[UUID] = Set.empty
           ): Group = {

    Group(
      displayName = displayName,
      ownerId = ownerId,
      contextId = contextId,
      allowedUsers = allowedUsers
    )

  }

}
