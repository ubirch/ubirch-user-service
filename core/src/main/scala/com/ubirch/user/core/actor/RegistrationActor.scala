package com.ubirch.user.core.actor

import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.RegistrationManager
import com.ubirch.user.model.rest.UserContext
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-04-20
  */
class RegistrationActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case register: RegisterUser =>

      val sender = context.sender()
      RegistrationManager.register(register.userContext).onComplete {

        case Success(userInfoOpt) =>

          log.debug(s"registration result: $userInfoOpt")
          userInfoOpt match {
            case None => sender ! None
            case Some(userInfo) => sender ! userInfo
          }

        case Failure(t) =>

          val errorMsg = JsonErrorResponse(errorType = "ValidationError", errorMessage = t.getMessage)
          log.debug(s"registration result: $errorMsg")
          sender ! errorMsg

      }

  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received from ${context.sender().path} unknown message: ${message.toString} (${message.getClass})")
    context.sender ! JsonErrorResponse(errorType = "UnknownMessage", errorMessage = "unable to handle message")
  }

}

object RegistrationActor {

  def props()(implicit mongo: MongoUtil): Props = {
    new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new RegistrationActor))
  }

}

case class RegisterUser(userContext: UserContext)
