package com.ubirch.user.testTools.external

import java.util.UUID

import com.ubirch.user.core.manager.{GroupManager, GroupsManager}
import com.ubirch.user.model.db.tools.DefaultModels
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-04-07
  */
class DataHelpers(implicit mongo: MongoUtil) {

  def createGroup(contextOpt: Option[Context],
                  ownerOpt: Option[User],
                  allowedUsersOpt: Option[User]*
                 ): Future[Option[Group]] = {

    if (contextOpt.isEmpty) {
      throw new Exception("failed to prepare context")
    }

    if (ownerOpt.isEmpty) {
      throw new Exception("failed to prepare owner")
    }

    for (userOpt <- allowedUsersOpt) {
      if (userOpt.isEmpty) {
        throw new Exception("failed to prepare allowedUsers")
      }
    }

    val allowedUsers: Seq[UUID] = allowedUsersOpt map { userOpt =>
      userOpt.get.id
    }
    val group = DefaultModels.group(
      ownerId = ownerOpt.get.id,
      contextId = contextOpt.get.id,
      allowedUsers = allowedUsers.toSet
    )

    GroupManager.create(group) map {
      case None => throw new Exception("failed to prepare group")
      case Some(g) => Some(g)
    }

  }

  def findGroup(context: Context, user: User): Future[Set[Group]] = {

    GroupsManager.findByContextAndUser(
      contextName = context.displayName,
      providerId = user.providerId,
      externalUserId = user.externalId
    )

  }

}
