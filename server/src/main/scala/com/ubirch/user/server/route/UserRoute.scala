package com.ubirch.user.server.route

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.actor.{ActorNames, CreateUser, DeleteUser, FindUser, UpdateUser, UserActor}
import com.ubirch.user.model.rest.User
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.{ActorSystem, Props}
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
trait UserRoute extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val userActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[UserActor]), ActorNames.USER)

  val route: Route = {

    pathPrefix(RouteConstants.user) {
      respondWithCORS {

        pathEnd {

          put {
            entity(as[User]) { user =>
              createUser(user)
            }
          } ~ post {
            entity(as[User]) { user =>
              updateUser(user)
            }
          }

        } ~ path(Segment / Segment) { (provider, userId) =>

          get {
            findByProviderUserId(provider, userId)
          }

        } ~ path(JavaUUID) { userId =>

          delete {
            deleteById(userId)
          }

        }

      }
    }

  }

  private def createUser(user: User): Route = {

    onComplete(userActor ? CreateUser(user)) {

      case Failure(t) =>
        logger.error("create user call responded with an unhandled message (check UserRoute for bugs!!!)")
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {
          case u: User => complete(u)
          case _ => complete(serverErrorResponse(errorType = "CreateError", errorMessage = "failed to create user"))
        }

    }

  }

  private def updateUser(user: User): Route = {

    onComplete(userActor ? UpdateUser(user)) {

      case Failure(t) =>
        logger.error("update user call responded with an unhandled message (check UserRoute for bugs!!!)")
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {
          case u: User => complete(u)
          case _ => complete(serverErrorResponse(errorType = "UpdateError", errorMessage = "failed to update user"))
        }

    }

  }

  private def findByProviderUserId(providerId: String, externalUserId: String): Route = {

    onComplete(userActor ? FindUser(providerId, externalUserId)) {

      case Failure(t) =>
        logger.error("findUser call responded with an unhandled message (check UserRoute for bugs!!!)")
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {
          case u: User => complete(u)
          case _ => complete(serverErrorResponse(errorType = "QueryError", errorMessage = "failed to query user"))
        }

    }

  }

  private def deleteById(userId: UUID): Route = {

    onComplete(userActor ? DeleteUser(userId)) {

      case Failure(t) =>
        logger.error("deleteUser call responded with an unhandled message (check UserRoute for bugs!!!)")
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {
          case u: User => complete(u)
          case _ => complete(serverErrorResponse(errorType = "DeleteError", errorMessage = "failed to delete user"))
        }

    }

  }

}
