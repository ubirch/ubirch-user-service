package com.ubirch.user.core.manager

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Group
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object GroupManager extends StrictLogging {

  private val collection = Config.mongoCollectionGroup

  def create(group: Group): Future[Option[Group]] = {

    // TODO implement
    Future(Some(group))

  }

  def update(group: Group): Future[Option[Group]] = {

    // TODO implement
    Future(Some(group))

  }

  def findById(id: UUID): Future[Option[Group]] = {

    // TODO implement
    Future(
      Some(
        Group(
          displayName = "displayName-find",
          ownerId = UUIDUtil.uuid,
          contextId = UUIDUtil.uuid,
          allowedUsers = Seq.empty
        )
      )
    )

  }

  def delete(id: UUID): Future[Boolean] = {

    // TODO implement
    Future(
      true
    )

  }

  def addAllowedUsers(groupId: UUID, allowedUsers: Seq[UUID]): Future[Boolean] = {

    // TODO implement
    Future(true)

  }

  def deleteAllowedUsers(groupId: UUID, allowedUsers: Seq[UUID]): Future[Boolean] = {

    // TODO implement
    Future(true)

  }

}
