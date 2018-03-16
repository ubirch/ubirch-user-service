package com.ubirch.user.server.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.core.actor._
import com.ubirch.user.model._
import com.ubirch.user.model.rest.User
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.Json4sUtil
import com.ubirch.util.model.{JsonErrorResponse, JsonResponse}
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
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

        } ~ path(RouteConstants.emailExists / Segment) { emailAddress =>

          get {
            searchByEmailAddress(emailAddress)
          }

        } ~ path(RouteConstants.hashedEmailExists / Segment) { hashedEmailAddress =>

          get {
            searchByHashedEmailAddress(hashedEmailAddress)
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

      case Success(resp) =>

        resp match {

          case Some(u: db.User) =>
            complete(StatusCodes.OK -> Json4sUtil.any2any[rest.User](u))

          case jer: JsonErrorResponse =>
            complete(StatusCodes.BadRequest -> jer)

          case _ =>
            complete(StatusCodes.InternalServerError -> serverErrorResponse(errorType = "CreateError", errorMessage = "failed to create user"))

        }

      case Failure(t) =>
        logger.error("create user failed", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))
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

      case Success(resp) =>
        resp match {

          case Some(u: db.User) =>
            complete(StatusCodes.OK -> Json4sUtil.any2any[rest.User](u))

          case jer: JsonErrorResponse =>
            complete(StatusCodes.BadRequest -> jer)

          case _ =>
            complete(serverErrorResponse(errorType = "UpdateError", errorMessage = "failed to update user"))

        }

      case Failure(t) =>
        logger.error("update user call responded with an unhandled message (check UserRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

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

  private def searchByEmailAddress(emailAddress: String): Route = {

    onComplete(userActor ? SearchByEmail(emailAddress)) {

      case Failure(t) =>
        logger.error("searchByEmail call responded with an unhandled message", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))

      case Success(resp) =>
        resp match {
          case true =>
            complete(StatusCodes.OK -> JsonResponse(message = s"email address exist: $emailAddress"))
          case false =>
            complete(StatusCodes.BadRequest -> JsonResponse(message = s"email does not address exist: $emailAddress"))
          case jre: JsonErrorResponse =>
            complete(StatusCodes.BadRequest -> jre)
          case _ =>
            val errMsg = s"no user with given email address exists: $emailAddress"
            logger.error(errMsg)
            complete(StatusCodes.BadRequest -> JsonErrorResponse(errorType = "QueryError", errorMessage = errMsg))
        }

    }

  }

  private def searchByHashedEmailAddress(hasehEmailAddress: String): Route = {

    onComplete(userActor ? SearchByHashedEmail(hasehEmailAddress)) {

      case Failure(t) =>
        logger.error("searchByHashedEmailAddress", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))

      case Success(resp) =>
        resp match {
          case true =>
            complete(StatusCodes.OK -> JsonResponse(message = s"hashed email address exist: $hasehEmailAddress"))
          case false =>
            complete(StatusCodes.BadRequest -> JsonResponse(message = s"hashed email does not address exist: $hasehEmailAddress"))
          case jre: JsonErrorResponse =>
            complete(StatusCodes.BadRequest -> jre)
          case _ =>
            complete(serverErrorResponse(errorType = "QueryError", errorMessage = "no user with given email address exists"))
        }

    }

  }

}
