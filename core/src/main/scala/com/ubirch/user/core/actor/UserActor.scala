package com.ubirch.user.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.UserManager
import com.ubirch.user.model.db.User
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class UserActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging {

  override def receive: Receive = {

    case create: CreateUser =>
      val sender = context.sender()

      val toCreate = create.user.copy(id = UUIDUtil.uuidStr)

      UserManager.create(toCreate).onComplete {
        case Success(u) =>
          sender ! u
        case Failure(t) =>
          sender ! JsonErrorResponse(errorType = "ValidationError", errorMessage = t.getMessage)
      }

    case update: UpdateUser =>

      val sender = context.sender()
      UserManager.findByProviderIdAndExternalId(update.providerId, externalUserId = update.externalUserId) map {

        case None =>
          val errMsg = s"unable to update user as it does not exist: provider=${update.providerId}, externalId=${update.externalUserId}"
          log.error(errMsg)
          sender ! JsonErrorResponse(errorType = "ValidationError", errorMessage = errMsg)

        case Some(u: User) =>

          val toUpdate = update.user.copy(id = u.id)

          UserManager.update(toUpdate).onComplete {
            case Success(u) =>
              sender ! u
            case Failure(t) =>
              sender ! JsonErrorResponse(errorType = "ValidationError", errorMessage = t.getMessage)
          }
      }

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

    case byEmail: SearchByEmail =>

      val sender = context.sender()
      UserManager.findByEmail(byEmail.emailAddress) map (sender ! _.isDefined)

    case byHashedEmail: SearchByHashedEmail =>

      val sender = context.sender()
      UserManager.findByHashedEmail(byHashedEmail.hashedEmailAddress) map (sender ! _.isDefined)


    case _ => log.error("unknown message")

  }

}

object UserActor {

  def props()(implicit mongo: MongoUtil): Props = {
    new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new UserActor))
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

case class SearchByEmail(emailAddress: String)

case class SearchByHashedEmail(hashedEmailAddress: String)
