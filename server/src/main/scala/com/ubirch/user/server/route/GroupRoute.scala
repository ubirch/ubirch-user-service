package com.ubirch.user.server.route

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.actor.{ActorNames, AddAllowedUsers, CreateGroup, DeleteAllowedUsers, DeleteGroup, FindGroup, FindMemberOf, FoundMemberOf, GroupActor, UpdateGroup}
import com.ubirch.user.model._
import com.ubirch.user.model.rest.{AllowedUsers, Group}
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class GroupRoute(implicit mongo: MongoUtil) extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val groupActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new GroupActor)), ActorNames.GROUP)

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
    onComplete(groupActor ? CreateGroup(dbGroup)) {

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
    onComplete(groupActor ? UpdateGroup(dbGroup)) {

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

    onComplete(groupActor ? FindGroup(groupId)) {

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

    onComplete(groupActor ? DeleteGroup(groupId)) {

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

    onComplete(groupActor ? AddAllowedUsers(groupId = allowedUsers.groupId, allowedUsers = allowedUsers.allowedUsers)) {

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

    onComplete(groupActor ? DeleteAllowedUsers(groupId = allowedUsers.groupId, allowedUsers = allowedUsers.allowedUsers)) {

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

    onComplete(groupActor ? FindMemberOf(
      contextName = contextName,
      providerId = providerId,
      externalUserId = externalUserId
    )
    ) {

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
