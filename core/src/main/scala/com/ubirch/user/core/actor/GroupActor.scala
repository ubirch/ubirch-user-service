package com.ubirch.user.core.actor

import java.util.UUID

import com.ubirch.user.core.manager.GroupManager
import com.ubirch.user.model.db.Group
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class GroupActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case create: CreateGroup =>
      val sender = context.sender()
      val toCreate = create.group.copy(id = UUIDUtil.uuid)
      GroupManager.create(toCreate) map (sender ! _)

    case update: UpdateGroup =>
      val sender = context.sender()
      GroupManager.update(update.group) map (sender ! _)

    case find: FindGroup =>
      val sender = context.sender()
      GroupManager.findById(find.id) map (sender ! _)

    case find: FindGroupByName =>
      val sender = context.sender()
      GroupManager.findByName(find.name) map (sender ! _)

    case delete: DeleteGroup =>
      val sender = context.sender()
      GroupManager.delete(delete.id) map (sender ! _)

    case delete: DeleteGroupByName =>
      val sender = context.sender()
      deleteByName(delete.name) map (sender ! _)

    case addAllowed: AddAllowedUsers =>
      val sender = context.sender()
      GroupManager.addAllowedUsers(
        groupId = addAllowed.groupId,
        allowedUsers = addAllowed.allowedUsers
      ) map (sender ! _)

    case deleteAllowed: DeleteAllowedUsers =>
      val sender = context.sender()
      GroupManager.deleteAllowedUsers(
        groupId = deleteAllowed.groupId,
        allowedUsers = deleteAllowed.allowedUsers
      ) map (sender ! _)

    case _ => log.error("unknown message")

  }

  private def deleteByName(name: String): Future[Boolean] = {

    GroupManager.findByName(name) flatMap {

      case None =>
        log.error("")
        Future(false)

      case Some(group) => GroupManager.delete(group.id)

    }

  }

}

case class CreateGroup(group: Group)

case class UpdateGroup(group: Group)

case class FindGroup(id: UUID)

case class FindGroupByName(name: String)

case class DeleteGroup(id: UUID)

case class DeleteGroupByName(name: String)

case class AddAllowedUsers(groupId: UUID,
                           allowedUsers: Set[UUID]
                          )

case class DeleteAllowedUsers(groupId: UUID,
                              allowedUsers: Set[UUID]
                             )
