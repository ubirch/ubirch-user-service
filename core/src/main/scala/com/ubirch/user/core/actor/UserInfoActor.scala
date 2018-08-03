package com.ubirch.user.core.actor

import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.UserInfoManager
import com.ubirch.user.model.rest.{SimpleUserContext, UpdateInfo, UserContext, UserUpdate}
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-04-25
  */
class UserInfoActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case simpleUserContext: SimpleUserContext =>

      val sender = context.sender()
      UserInfoManager.getInfo(simpleUserContext) map (sender ! _)

    case update: UpdateInfo =>

      val sender = context.sender()
      UserInfoManager.update(update.simpleUserContext, update.update) map (sender ! _)

  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received from ${context.sender().path} unknown message: ${message.toString} (${message.getClass})")
    context.sender ! JsonErrorResponse(errorType = "UnknownMessage", errorMessage = "unable to handle message")
  }

}

object UserInfoActor {

  def props()(implicit mongo: MongoUtil): Props = {
    new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new UserInfoActor))
  }

}
