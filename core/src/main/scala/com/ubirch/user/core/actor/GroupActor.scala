package com.ubirch.user.core.actor

import java.util.UUID

import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.{GroupManager, GroupsManager}
import com.ubirch.user.model.db.Group
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class GroupActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case create: CreateGroup =>
      val sender = context.sender()
      val toCreate = create.group.copy(id = UUIDUtil.uuidStr)
      GroupManager.create(toCreate) map (sender ! _)

    case update: UpdateGroup =>
      val sender = context.sender()
      GroupManager.update(update.group) map (sender ! _)

    case find: FindGroup =>
      val sender = context.sender()
      GroupManager.findById(find.id.toString) map (sender ! _)

    case delete: DeleteGroup =>
      val sender = context.sender()
      GroupManager.delete(delete.id.toString) map (sender ! _)

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

    case find: FindMemberOf =>

      val sender = context.sender()
      GroupsManager.findByContextAndUser(
        contextName = find.contextName,
        providerId = find.providerId,
        externalUserId = find.externalUserId
      ) map (sender ! FoundMemberOf(_))

  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received from ${context.sender().path} unknown message: ${message.toString} (${message.getClass})")
    context.sender() ! JsonErrorResponse(errorType = "ServerError", errorMessage = "Berlin, we have a problem!")
  }

}

object GroupActor {

  def props()(implicit mongo: MongoUtil): Props = {
    new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new GroupActor))
  }

}

case class CreateGroup(group: Group)

case class UpdateGroup(group: Group)

case class FindGroup(id: UUID)

case class DeleteGroup(id: UUID)

case class AddAllowedUsers(groupId: UUID,
                           allowedUsers: Set[UUID]
                          )

case class DeleteAllowedUsers(groupId: UUID,
                              allowedUsers: Set[UUID]
                             )

case class FindMemberOf(contextName: String,
                        providerId: String,
                        externalUserId: String
                       )

case class FoundMemberOf(groups: Set[Group])
