package com.ubirch.user.server.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.actor.{ActorNames, UserInfoActor}
import com.ubirch.user.model.rest.{SimpleUserContext, UpdateInfo, UserInfo}
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.actor.ActorSystem
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
  * since: 2017-04-25
  */
class UserInfoRoute(implicit mongo: MongoUtil)
  extends ResponseUtil
    with CORSDirective
    with StrictLogging {

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val userInfoActor = system.actorOf(UserInfoActor.props(), ActorNames.USER_INFO)

  val route: Route = {

    // TODO fix: all userInfo routes fail with 404
    pathPrefix(RouteConstants.userInfo) {

      path(Segment / Segment / Segment) { (context, providerId, userId) =>
        respondWithCORS {

          get {
            val simpleUserContext = SimpleUserContext(
              context = context,
              providerId = providerId,
              userId = userId
            )
            getInfo(simpleUserContext)
          }

        }
      } ~ pathEnd {
        respondWithCORS {

          entity(as[UpdateInfo]) { updateInfo =>
            put {
              update(updateInfo)
            }
          }

        }
      }

    }

  }

  private def getInfo(simpleUserContext: SimpleUserContext): Route = {

    onComplete(userInfoActor ? simpleUserContext) {

      case Failure(t) =>
        logger.error("get-user call responded with an unhandled message (check UserInfoRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>

        resp match {

          case Some(userInfo: UserInfo) => complete(userInfo)

          case None =>
            logger.error("failed to get user info (None)")
            complete(requestErrorResponse(errorType = "NoUserInfoFound", errorMessage = "failed to get user info"))

          case _ =>
            logger.error("failed to get user info (server error)")
            complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to get user info"))

        }

    }

  }

  private def update(updateInfo: UpdateInfo): Route = {

    onComplete(userInfoActor ? updateInfo) {

      case Failure(t) =>
        logger.error("update-user call responded with an unhandled message (check UserInfoRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>

        resp match {

          case Some(userInfo: UserInfo) => complete(userInfo)

          case None =>
            logger.error("failed to update user info (None)")
            complete(requestErrorResponse(errorType = "UpdateError", errorMessage = "failed to update user info"))

          case _ =>
            logger.error("failed to update user info (server error)")
            complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to update user info"))

        }

    }

  }

}
