package com.ubirch.user.core.actor

import java.util.UUID

import com.ubirch.user.core.manager.ContextManager
import com.ubirch.user.model.db.Context
import com.ubirch.util.mongo.connection.MongoUtil

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-03-29
  */
class ContextActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case create: CreateContext =>
      val sender = context.sender()
      ContextManager.create(create.context) map (sender ! _)

    case update: UpdateContext =>
      val sender = context.sender()
      ContextManager.update(update.context) map (sender ! _)

    case get: GetContext =>
      val sender = context.sender()
      ContextManager.findById(get.id) map (sender ! _)

    case find: FindContextByName =>
      val sender = context.sender()
      ContextManager.findByName(find.name) map (sender ! _)

    case delete: DeleteContext =>
      val sender = context.sender()
      ContextManager.delete(delete.id) map (sender ! _)

    case _ => log.error("unknown message")

  }

}

case class CreateContext(context: Context)

case class UpdateContext(context: Context)

case class GetContext(id: UUID)

case class DeleteContext(id: UUID)

case class FindContextByName(name: String)
