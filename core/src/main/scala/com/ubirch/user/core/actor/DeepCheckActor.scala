package com.ubirch.user.core.actor

import com.ubirch.util.model.DeepCheckResponse
import com.ubirch.util.mongo.connection.MongoUtil

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-06
  */
class DeepCheckActor(implicit mongo: MongoUtil)
  extends Actor
    with ActorLogging {

  override def receive: Receive = {

    case _: DeepCheckRequest =>
      val sender = context.sender()
      deepCheck() map (sender ! _)

    case _ => log.error("unknown message")

  }

  private def deepCheck(): Future[DeepCheckResponse] = {
    // TODO check MongoDb connection
    Future(DeepCheckResponse())
  }

}

case class DeepCheckRequest()
