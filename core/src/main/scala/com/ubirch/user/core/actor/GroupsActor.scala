package com.ubirch.user.core.actor

import com.ubirch.user.core.manager.GroupsManager
import com.ubirch.user.model.rest.Group

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class GroupsActor extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case find: FindGroups =>

      val sender = context.sender()
      GroupsManager.findByContextNameAndExternalUserId(
        contextName = find.contextName,
        externalUserId = find.externalUserId
      ) map (sender ! FoundGroups(_))

    case _ => log.error("unknown message")

  }

}

case class FindGroups(contextName: String,
                      externalUserId: String
                     )

case class FoundGroups(groups: Seq[Group]) // TODO refactor to accept object from model-db
