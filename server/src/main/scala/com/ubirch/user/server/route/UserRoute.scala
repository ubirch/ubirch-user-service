package com.ubirch.user.server.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.actor.{ActorNames, CreateUser, DeleteUser, FindUser, UpdateUser, UserActor}
import com.ubirch.user.model._
import com.ubirch.user.model.rest.User
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.model.JsonErrorResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
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
class UserRoute(implicit mongo: MongoUtil) extends CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val userActor = system.actorOf(UserActor.props(), ActorNames.USER)

  val route: Route = {

    pathPrefix(RouteConstants.user) {
      respondWithCORS {

        pathEnd {

          post {
            entity(as[User]) { user =>
              createUser(user)
            }
          }

        } ~ path(Segment / Segment) { (provider, externalUserId) =>

          get {
            findByProviderUserId(provider, externalUserId)
          } ~ put {
            entity(as[User]) { user =>
              updateUser(provider, externalUserId, user)
            }
          } ~ delete {
            deleteById(provider, externalUserId)
          }

        }

      }
    }

  }

  private def createUser(restUser: User): Route = {

    val dbUser = Json4sUtil.any2any[db.User](restUser)
    onComplete(userActor ? CreateUser(dbUser)) {

      case Failure(t) =>
        logger.error("create user call responded with an unhandled message (check UserRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>

        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "CreateError", errorMessage = "user already exists")
            complete(requestErrorResponse(jsonError))

          case Some(u: db.User) => complete(Json4sUtil.any2any[rest.User](u))

          case _ => complete(serverErrorResponse(errorType = "CreateError", errorMessage = "failed to create user"))

        }

    }

  }

  private def updateUser(providerId: String,
                         externalUserId: String,
                         restUser: User
                        ): Route = {

    val dbUser = Json4sUtil.any2any[db.User](restUser)
    onComplete(userActor ? UpdateUser(
      providerId = providerId,
      externalUserId = externalUserId,
      dbUser
    )
    ) {

      case Failure(t) =>
        logger.error("update user call responded with an unhandled message (check UserRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "UpdateError", errorMessage = "failed to update user")
            complete(requestErrorResponse(jsonError))

          case Some(u: db.User) => complete(Json4sUtil.any2any[rest.User](u))

          case _ => complete(serverErrorResponse(errorType = "UpdateError", errorMessage = "failed to update user"))

        }

    }

  }

  private def findByProviderUserId(providerId: String, externalUserId: String): Route = {

    onComplete(userActor ? FindUser(providerId, externalUserId)) {

      case Failure(t) =>
        logger.error("findUser call responded with an unhandled message (check UserRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "QueryError", errorMessage = "failed to find user")
            complete(requestErrorResponse(jsonError))

          case Some(u: db.User) => complete(Json4sUtil.any2any[rest.User](u))

          case _ => complete(serverErrorResponse(errorType = "QueryError", errorMessage = "failed to query user"))

        }

    }

  }

  private def deleteById(providerId: String, externalUserId: String): Route = {

    onComplete(userActor ? DeleteUser(providerId = providerId, externalUserId = externalUserId)) {

      case Failure(t) =>
        logger.error("deleteUser call responded with an unhandled message (check UserRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {
          case deleted: Boolean if deleted => complete(StatusCodes.OK)
          case _ => complete(serverErrorResponse(errorType = "DeleteError", errorMessage = "failed to delete user"))
        }

    }

  }

}
