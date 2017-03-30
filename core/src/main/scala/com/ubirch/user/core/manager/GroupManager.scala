package com.ubirch.user.core.manager

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.rest.Group
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object GroupManager extends StrictLogging {

  def create(groupRest: Group): Future[Group] = {

    // TODO implement
    Future(groupRest.copy(id = Some(UUIDUtil.uuid)))

  }

  def update(groupRest: Group): Future[Group] = {

    // TODO implement
    Future(groupRest)

  }

  def findById(id: UUID): Future[Group] = {

    // TODO implement
    Future(
      Group(
        id = Some(id),
        displayName = "displayName-find",
        ownerId = UUIDUtil.uuid,
        contextId = UUIDUtil.uuid,
        allowedUsers = Seq.empty
      )
    )

  }

  def delete(id: UUID): Future[Group] = {

    // TODO implement
    Future(
      Group(
        id = Some(id),
        displayName = "displayName-delete",
        ownerId = UUIDUtil.uuid,
        contextId = UUIDUtil.uuid,
        allowedUsers = Seq.empty
      )
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
