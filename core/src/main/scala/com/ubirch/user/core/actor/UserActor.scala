package com.ubirch.user.core.actor

import com.ubirch.user.core.manager.UserManager
import com.ubirch.user.model.db.User
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil

import akka.actor.{Actor, ActorLogging}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class UserActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case create: CreateUser =>
      val sender = context.sender()
      val toCreate = create.user.copy(id = UUIDUtil.uuid)
      UserManager.create(toCreate) map (sender ! _)

    case update: UpdateUser =>

      val sender = context.sender()
      val updated = UserManager.findByProviderIdAndExternalId(update.providerId, externalUserId = update.externalUserId) flatMap {

        case None =>
          log.error(s"unable to update use as it does not exist: provider=${update.providerId}, externalId=${update.externalUserId}")
          Future(None)

        case Some(u: User) =>
          val toUpdate = update.user.copy(id = u.id)
          UserManager.update(toUpdate)

      }
      updated map (sender ! _)

    case find: FindUser =>
      val sender = context.sender()
      UserManager.findByProviderIdAndExternalId(
        providerId = find.providerId,
        externalUserId = find.externalUserId
      ) map (sender ! _)

    case delete: DeleteUser =>

      val sender = context.sender()

      val result = UserManager.findByProviderIdAndExternalId(
        providerId = delete.providerId,
        externalUserId = delete.externalUserId
      ) flatMap {

        case None =>
          log.error("unable to delete non-existing user")
          Future(false)

        case Some(u: User) => UserManager.delete(u.id)

      }
      result map (sender ! _)

    case _ => log.error("unknown message")

  }

}

case class CreateUser(user: User)

case class UpdateUser(providerId: String,
                      externalUserId: String,
                      user: User
                     )

case class FindUser(providerId: String,
                    externalUserId: String
                   )

case class DeleteUser(providerId: String,
                      externalUserId: String
                     )
