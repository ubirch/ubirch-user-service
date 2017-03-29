package com.ubirch.user.core.actor

import java.util.UUID

import com.ubirch.user.model.rest.Context
import com.ubirch.util.uuid.UUIDUtil

import akka.actor.{Actor, ActorLogging}

/**
  * author: cvandrei
  * since: 2017-03-29
  */
class ContextActor extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case create: CreateContext =>
      val sender = context.sender()
      sender ! create.context.copy(id = Some(UUIDUtil.uuid)) // TODO call manager

    case update: UpdateContext =>
      val sender = context.sender()
      sender ! update.context // TODO call manager

    case get: GetContext =>
      val sender = context.sender()
      sender ! Context(Some(get.id), "foo-display-name-get") // TODO call manager

    case delete: DeleteContext =>
      val sender = context.sender()
      sender ! Context(Some(delete.id), "foo-display-name-delete") // TODO call manager

    case _ => log.error("unknown message")

  }

}

case class CreateContext(context: Context)

case class UpdateContext(context: Context)

case class GetContext(id: UUID)

case class DeleteContext(id: UUID)
