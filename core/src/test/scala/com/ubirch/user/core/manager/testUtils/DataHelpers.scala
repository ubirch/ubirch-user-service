package com.ubirch.user.core.manager.testUtils

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.core.manager.{ContextManager, GroupManager, GroupsManager, UserManager}
import com.ubirch.user.model.db.tools.DefaultModels
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

/**
  * author: cvandrei
  * since: 2017-04-07
  */
class DataHelpers(implicit mongo: MongoUtil) extends StrictLogging {

  def createUser(id: String = UUIDUtil.uuidStr,
                 displayName: String = s"automated-test-${UUIDUtil.uuid}",
                 providerId: String = "google",
                 externalId: String = s"${Random.nextLong()}",
                 locale: String = "en",
                 created: DateTime = DateTime.now(DateTimeZone.UTC),
                 updated: DateTime = DateTime.now(DateTimeZone.UTC)
                ): Future[Option[User]] = {

    val user = User(
      id = id,
      displayName = displayName,
      providerId = providerId,
      externalId = externalId,
      locale = locale,
      created = created,
      updated = updated
    )

    UserManager.create(user)

  }

  def createContext(id: String = UUIDUtil.uuidStr,
                    displayName: String = s"automated-test-${UUIDUtil.uuid}",
                    created: DateTime = DateTime.now(DateTimeZone.UTC),
                    updated: DateTime = DateTime.now(DateTimeZone.UTC)
                   ): Future[Option[Context]] = {

    val context = Context(
      id = id,
      displayName = displayName,
      created = created,
      updated = updated
    )

    ContextManager.create(context)

  }

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

    val allowedUsers: Seq[String] = allowedUsersOpt map { userOpt =>
      userOpt.get.id
    }
    val group = DefaultModels.group(
      ownerIds = Set(ownerOpt.get.id),
      contextId = contextOpt.get.id,
      allowedUsers = allowedUsers.toSet
    )

    GroupManager.create(group) map {
      case None => throw new Exception("failed to prepare group")
      case Some(g) => Some(g)
    }

  }

  def createGroup(contextOpt: Option[Context],
                  ownerOpt: Option[User],
                  adminGroup: Option[Boolean],
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

    logger.info(s"== trying to create group: contextName=${contextOpt.get.displayName}, owner:${ownerOpt.get}")
    createGroup(
      contextId = contextOpt.get.id,
      ownerOpt = ownerOpt,
      adminGroup = adminGroup,
      allowedUsersOpt = allowedUsersOpt: _*
    )

  }

  def createGroup(contextId: String,
                  ownerOpt: Option[User],
                  adminGroup: Option[Boolean],
                  allowedUsersOpt: Option[User]*
                 ): Future[Option[Group]] = {

    if (ownerOpt.isEmpty) {
      throw new Exception("failed to prepare owner")
    }

    for (userOpt <- allowedUsersOpt) {
      if (userOpt.isEmpty) {
        throw new Exception("failed to prepare allowedUsers")
      }
    }

    val allowedUsers: Seq[String] = allowedUsersOpt map { userOpt =>
      userOpt.get.id
    }
    val group = DefaultModels.group(
      ownerIds = Set(ownerOpt.get.id),
      contextId = contextId,
      allowedUsers = allowedUsers.toSet,
      adminGroup = adminGroup
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
