package com.ubirch.user.core.actor

import java.util.UUID

import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.ContextManager
import com.ubirch.user.model.db.Context
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool

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
      val toCreate = create.context.copy(id = UUIDUtil.uuidStr)
      ContextManager.create(toCreate) map (sender ! _)

    case update: UpdateContext =>
      val sender = context.sender()
      ContextManager.update(update.context) map (sender ! _)

    case get: GetContext =>
      val sender = context.sender()
      ContextManager.findById(get.id.toString) map (sender ! _)

    case find: FindContextByName =>
      val sender = context.sender()
      ContextManager.findByName(find.name) map (sender ! _)

    case delete: DeleteContext =>
      val sender = context.sender()
      ContextManager.delete(delete.id.toString) map (sender ! _)

  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received from ${context.sender().path} unknown message: ${message.toString} (${message.getClass})")
    context.sender() ! JsonErrorResponse(errorType = "ServerError", errorMessage = "Berlin, we have a problem!")
  }

}

object ContextActor {

  def props()(implicit mongo: MongoUtil): Props = {
    new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new ContextActor))
  }

}

case class CreateContext(context: Context)

case class UpdateContext(context: Context)

case class GetContext(id: UUID)

case class DeleteContext(id: UUID)

case class FindContextByName(name: String)
