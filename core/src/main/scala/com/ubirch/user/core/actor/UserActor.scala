package com.ubirch.user.core.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.routing.RoundRobinPool
import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.UserManager
import com.ubirch.user.model.db.User
import com.ubirch.user.model.rest.ActivationUpdate
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.uuid.UUIDUtil
import org.joda.time.DateTime

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class UserActor(implicit mongo: MongoUtil) extends Actor
  with ActorLogging with UserActorLogic {

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

    case toRestore: RestoreUser =>

      val sender = context.sender()
      UserManager.create(toRestore.user).onComplete {
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
            case Success(updated) =>
              sender ! updated
            case Failure(t) =>
              sender ! JsonErrorResponse(errorType = "ValidationError", errorMessage = t.getMessage)
          }

      }

    case find: FindUser =>

      log.debug(s"UserActor.FindUser -- find=$find")
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
          Future.successful(false)

        case Some(u: User) => UserManager.delete(u.id)

      }
      result map (sender ! _)

    case byExternalId: SearchByExternalId =>

      val sender = context.sender()
      UserManager.findByExternalId(byExternalId.emailAddress).onComplete {
        case Success(u) =>
          sender ! u.isDefined
        case Failure(t) =>
          sender ! JsonErrorResponse(
            errorType = "ValidationError",
            errorMessage = t.getMessage
          )

      }

    case updates: ActivationUpdate =>

      val sender = context.sender()
      updateActivation(updates)
        .onComplete {
          case Success(rsp) =>
            sender ! rsp
          case Failure(t) =>
            sender ! JsonErrorResponse(
              errorType = "InternalServerError",
              errorMessage = t.getMessage
            )
        }

    case getUsersWithCursor: GetUsersWithCursor =>
      val sender = context.sender()
      UserManager.getWithCursor(getUsersWithCursor.limit, getUsersWithCursor.lastCreatedAt) map (sender ! _)

    case getUsersWithOffset: GetUsersWithOffset =>
      val sender = context.sender()
      UserManager.getWithOffset(getUsersWithOffset.limit, getUsersWithOffset.offset) map (sender ! _)

    case getUsersByIds: GetUsersByIds =>
      val sender = context.sender()
      UserManager.findByUsedIds(getUsersByIds.userIds) map (sender ! _)
  }

  override def unhandled(message: Any): Unit = {
    log.error(s"received from ${context.sender().path} unknown message: ${message.toString} (${message.getClass})")
    context.sender() ! JsonErrorResponse(errorType = "ServerError", errorMessage = "Berlin, we have a problem!")
  }

}

object UserActor {

  def props()(implicit mongo: MongoUtil): Props = {
    new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new UserActor))
  }

}

case class CreateUser(user: User)

case class RestoreUser(user: User)

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

case class SearchByExternalId(emailAddress: String)

case class SearchByHashedEmail(hashedEmailAddress: String)

case class GetUsersWithCursor(limit: Int, lastCreatedAt: Option[DateTime])

case class GetUsersWithOffset(limit: Int, offset: Option[Int])

case class GetUsersByIds(userIds: List[UUID])
