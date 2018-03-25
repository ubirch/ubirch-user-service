package com.ubirch.user.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.DeepCheckManager
import com.ubirch.util.deepCheck.model.{DeepCheckRequest, DeepCheckResponse}
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

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
      deepCheck().onComplete {
        case Success(res) =>
          sender ! res
        case Failure(t) =>
          sender ! JsonErrorResponse(
            errorType = "CheckError",
            errorMessage = t.getMessage
          )
      }

    case _ => log.error("unknown message")

  }

  private def deepCheck(): Future[DeepCheckResponse] = DeepCheckManager.connectivityCheck()

}

object DeepCheckActor {

  def props()(implicit mongo: MongoUtil): Props = {
    new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new DeepCheckActor))
  }

}
