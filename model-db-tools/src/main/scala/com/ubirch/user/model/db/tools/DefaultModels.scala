package com.ubirch.user.model.db.tools

import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.util.date.DateUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2017-04-06
  */
object DefaultModels {

  def context(displayName: String = s"automated-test-${UUIDUtil.uuidStr}"): Context = {

    Context(
      displayName = displayName
    )

  }

  def user(displayName: String = "automated-test-user",
           providerId: String = "google",
           externalId: String = UUIDUtil.uuidStr,
           locale: String = "en",
           email: Option[String] = None,
           created: DateTime = DateUtil.nowUTC
          ): User = {

    User(
      displayName = displayName,
      providerId = providerId,
      externalId = externalId,
      locale = locale,
      email = email,
      created = created,
      updated = created
    )

  }

  def group(displayName: String = s"group-${UUIDUtil.uuid}",
            ownerIds: Set[String] = Set(UUIDUtil.uuidStr),
            contextId: String = UUIDUtil.uuidStr,
            allowedUsers: Set[String] = Set.empty,
            adminGroup: Option[Boolean] = None
           ): Group = {

    Group(
      displayName = displayName,
      ownerIds = ownerIds,
      contextId = contextId,
      allowedUsers = allowedUsers,
      adminGroup = adminGroup
    )

  }

}
