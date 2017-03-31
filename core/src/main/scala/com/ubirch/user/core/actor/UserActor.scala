package com.ubirch.user.core.actor

import java.util.UUID

import com.ubirch.user.core.manager.UserManager
import com.ubirch.user.model.db.User

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class UserActor extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case create: CreateUser =>
      val sender = context.sender()
      UserManager.create(create.user) map (sender ! _)

    case update: UpdateUser =>
      val sender = context.sender()
      UserManager.update(update.user) map (sender ! _)

    case find: FindUser =>
      val sender = context.sender()
      UserManager.findByProviderIdExternalId(providerId = find.providerId, externalUserId = find.externalUserId) map (sender ! _)

    case delete: DeleteUser =>
      val sender = context.sender()
      UserManager.delete(delete.userId) map (sender ! _)

    case _ => log.error("unknown message")

  }

}

case class CreateUser(user: User)

case class UpdateUser(user: User)

case class FindUser(providerId: String, externalUserId: String)

case class DeleteUser(userId: UUID)
