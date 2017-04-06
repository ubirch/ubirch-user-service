package com.ubirch.user.core.manager

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.db.Group
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object GroupsManager extends StrictLogging {

  def findByContextAndUser(contextName: String,
                           providerId: String,
                           externalUserId: String
                                        ): Future[Seq[Group]] = {

    // TODO implement
    val ownerId = UUIDUtil.uuid
    val contextId = UUIDUtil.uuid
    val allowedUser1 = UUIDUtil.uuid
    val allowedUser2 = UUIDUtil.uuid

    Future(
      Seq(
        Group(
          displayName = "display-name-group-1",
          ownerId = ownerId,
          contextId = contextId,
          allowedUsers = Seq(allowedUser1, allowedUser2)
        ),
        Group(
          displayName = "display-name-group-2",
          ownerId = ownerId,
          contextId = contextId,
          allowedUsers = Seq(allowedUser1)
        )
      )
    )

  }

}
