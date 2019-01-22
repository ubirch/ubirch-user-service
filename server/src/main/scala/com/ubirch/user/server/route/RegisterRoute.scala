package com.ubirch.user.server.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.actor.{ActorNames, RegisterUser, RegistrationActor}
import com.ubirch.user.model.rest.{UserContext, UserInfo}
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.model.JsonErrorResponse
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
  * since: 2017-04-20
  */
class RegisterRoute(implicit mongo: MongoUtil, val system: ActorSystem)
  extends ResponseUtil
    with WithRoutesHelpers
    with CORSDirective
    with StrictLogging {

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val registrationActor = system.actorOf(RegistrationActor.props(), ActorNames.REGISTRATION)

  val route: Route = {

    path(RouteConstants.register) {
      respondWithCORS {
        entity(as[UserContext]) { userContext =>

          logger.debug(s"userContext=$userContext")
          post {
            registerUser(userContext)
          }

        }
      }
    }

  }

  private def registerUser(userContext: UserContext): Route = {

    OnComplete(registrationActor ? RegisterUser(userContext)).fold() {

      case Failure(t) =>

        logger.error("register user call responded with an unhandled message: ", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))

      case Success(resp) =>

        resp match {

          case userInfo: UserInfo =>

            logger.debug(s"RegisterRoute: registered user? userInfo=$userInfo")
            complete(userInfo)

          case None =>

            logger.error(s"failed to register user (None) (userContext=$userContext)")
            complete(requestErrorResponse(errorType = "RegistrationError", errorMessage = "failed to register user"))

          case jsonError: JsonErrorResponse =>

            logger.error(s"failed to register user: jsonError=$jsonError")
            complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to register user"))

          case _ =>

            logger.error("failed to register user (server error)")
            complete(serverErrorResponse(errorType = "ServerError", errorMessage = "failed to register user"))

        }

    }

  }

}
