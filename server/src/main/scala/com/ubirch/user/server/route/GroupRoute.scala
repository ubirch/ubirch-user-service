package com.ubirch.user.server.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.core.actor._
import com.ubirch.user.model._
import com.ubirch.user.model.rest.{AllowedUsers, Group}
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import java.util.UUID
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success}
/**
  * author: cvandrei
  * since: 2017-03-30
  */
class GroupRoute(implicit mongo: MongoUtil, val system: ActorSystem) extends CORSDirective
  with ResponseUtil
  with WithRoutesHelpers
  with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val groupActor = system.actorOf(GroupActor.props(), ActorNames.GROUP)

  val route: Route = {

    pathPrefix(RouteConstants.group) {
      respondWithCORS {

        pathEnd {

          post {
            entity(as[Group]) { group =>
              createGroup(group)
            }
          } ~ put {
            entity(as[Group]) { group =>
              updateGroup(group)
            }
          }

        } ~ path(JavaUUID) { groupId =>

          get {
            findById(groupId)
          } ~ delete {
            deleteById(groupId)
          }

        } ~ path(RouteConstants.allowedUsers) {

          put {
            entity(as[AllowedUsers]) { allowedUsers =>
              addAllowedUsers(allowedUsers)
            }
          } ~ delete {
            entity(as[AllowedUsers]) { allowedUsers =>
              deleteAllowedUsers(allowedUsers)
            }
          }

        } ~ path(RouteConstants.memberOf / Segment / Segment / Segment) { (contextName, providerId, externalUserId) =>

          get {
            findByContextNameAndExternalUserId(
              contextName = contextName,
              providerId = providerId,
              externalUserId = externalUserId
            )
          }

        }

      }
    }

  }

  private def createGroup(restGroup: Group): Route = {

    val dbGroup = Json4sUtil.any2any[db.Group](restGroup)

    OnComplete(groupActor ? CreateGroup(dbGroup)).fold() {

      case Failure(t) =>
        logger.error("create user call responded with an unhandled message (check GroupRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "QueryError", errorMessage = "failed to create group")
            complete(requestErrorResponse(jsonError))

          case Some(g: db.Group) => complete(Json4sUtil.any2any[rest.Group](g))

          case _ => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to create group"))

        }

    }

  }

  private def updateGroup(restGroup: Group): Route = {

    val dbGroup = Json4sUtil.any2any[db.Group](restGroup)

    OnComplete(groupActor ? UpdateGroup(dbGroup)).fold() {

      case Failure(t) =>
        logger.error("update restGroup call responded with an unhandled message (check GroupRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "QueryError", errorMessage = "failed to update group")
            complete(requestErrorResponse(jsonError))

          case Some(g: db.Group) => complete(Json4sUtil.any2any[rest.Group](g))

          case _ => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to update group"))

        }

    }

  }

  private def findById(groupId: UUID): Route = {

    OnComplete(groupActor ? FindGroup(groupId)).fold() {

      case Failure(t) =>
        logger.error("findGroup call responded with an unhandled message (check GroupRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "QueryError", errorMessage = "failed to find group")
            complete(requestErrorResponse(jsonError))

          case Some(g: db.Group) => complete(Json4sUtil.any2any[rest.Group](g))

          case _ => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to query group"))

        }

    }

  }

  private def deleteById(groupId: UUID): Route = {

    OnComplete(groupActor ? DeleteGroup(groupId)).fold() {

      case Failure(t) =>
        logger.error("deleteGroup call responded with an unhandled message (check GroupRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case deleted: Boolean if deleted => complete(StatusCodes.OK)

          case deleted: Boolean if !deleted =>
            val jsonError = JsonErrorResponse(errorType = "DeleteError", errorMessage = "failed to delete group")
            complete(requestErrorResponse(jsonError))

          case _ => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to delete group"))

        }

    }

  }

  private def addAllowedUsers(allowedUsers: AllowedUsers): Route = {

    OnComplete(groupActor ? AddAllowedUsers(groupId = allowedUsers.groupId, allowedUsers = allowedUsers.allowedUsers)).fold() {

      case Failure(t) =>
        logger.error("adding allowed users call responded with an unhandled message (check GroupRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case b: Boolean if b => complete(StatusCodes.OK)

          case b: Boolean if !b =>
            val jsonError = JsonErrorResponse(errorType = "UpdateError", errorMessage = "failed to add allowed users")
            complete(requestErrorResponse(jsonError))

          case _ => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to add allowed users"))

        }

    }

  }

  private def deleteAllowedUsers(allowedUsers: AllowedUsers): Route = {

    OnComplete(groupActor ? DeleteAllowedUsers(groupId = allowedUsers.groupId, allowedUsers = allowedUsers.allowedUsers)).fold() {

      case Failure(t) =>
        logger.error("adding allowed users call responded with an unhandled message (check GroupRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case b: Boolean if b => complete(StatusCodes.OK)

          case b: Boolean if !b =>
            val jsonError = JsonErrorResponse(errorType = "UpdateError", errorMessage = "failed to delete allowed users")
            complete(requestErrorResponse(jsonError))

          case _ => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to add allowed users"))

        }

    }

  }

  private def findByContextNameAndExternalUserId(contextName: String,
                                                 providerId: String,
                                                 externalUserId: String
                                                ): Route = {

    OnComplete(groupActor ? FindMemberOf(
      contextName = contextName,
      providerId = providerId,
      externalUserId = externalUserId)
    ).fold() {

      case Failure(t) =>
        logger.error("findGroups call responded with an unhandled message (check GroupsRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case found: FoundMemberOf =>
            val restGroups = found.groups map Json4sUtil.any2any[rest.Group]
            complete(restGroups)

          case _ => complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to query groups"))

        }

    }

  }

}
