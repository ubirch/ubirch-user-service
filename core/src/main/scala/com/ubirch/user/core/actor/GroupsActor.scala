package com.ubirch.user.core.actor

import com.ubirch.user.core.manager.GroupsManager
import com.ubirch.user.model.db.Group
import com.ubirch.util.mongo.connection.MongoUtil

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class GroupsActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case find: FindGroups =>

      val sender = context.sender()
      GroupsManager.findByContextAndUser(
        contextName = find.contextName,
        providerId = find.providerId,
        externalUserId = find.externalUserId
      ) map (sender ! FoundGroups(_))

    case _ => log.error("unknown message")

  }

}

case class FindGroups(contextName: String,
                      providerId: String,
                      externalUserId: String
                     )

case class FoundGroups(groups: Set[Group])
