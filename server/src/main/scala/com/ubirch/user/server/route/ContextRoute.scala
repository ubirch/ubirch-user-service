package com.ubirch.user.server.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.actor.{ActorNames, ContextActor, CreateContext, DeleteContext, GetContext, UpdateContext}
import com.ubirch.user.model.rest.Context
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
  * since: 2017-03-29
  */
trait ContextRoute extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val contextActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[ContextActor]), ActorNames.CONTEXT)

  val route: Route = {

    pathPrefix(RouteConstants.context) {
      respondWithCORS {

        pathEnd {

          put {
            entity(as[Context]) { context =>

              onComplete(contextActor ? CreateContext(context)) {

                case Failure(t) =>
                  logger.error("create context call responded with an unhandled message (check ContextRoute for bugs!!!)")
                  complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

                case Success(resp) =>
                  resp match {
                    case c: Context => complete(c)
                    case _ => complete(serverErrorResponse(errorType = "CreateError", errorMessage = "failed to create context"))
                  }

              }

            }
          } ~ post {
            entity(as[Context]) { context =>

              onComplete(contextActor ? UpdateContext(context)) {

                case Failure(t) =>
                  logger.error("update context call responded with an unhandled message (check ContextRoute for bugs!!!)")
                  complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

                case Success(resp) =>
                  resp match {
                    case c: Context => complete(c)
                    case _ => complete(serverErrorResponse(errorType = "UpdateError", errorMessage = "failed to update context"))
                  }

              }

            }
          }

        } ~ path(JavaUUID) { contextId =>

          get {

            onComplete(contextActor ? GetContext(contextId)) {

              case Failure(t) =>
                logger.error("getContext call responded with an unhandled message (check ContextRoute for bugs!!!)")
                complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

              case Success(resp) =>
                resp match {
                  case c: Context => complete(c)
                  case _ => complete(serverErrorResponse(errorType = "QueryError", errorMessage = "failed to query context"))
                }

            }

          } ~ delete {

            onComplete(contextActor ? DeleteContext(contextId)) {

              case Failure(t) =>
                logger.error("deleteContext call responded with an unhandled message (check ContextRoute for bugs!!!)")
                complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

              case Success(resp) =>
                resp match {
                  case c: Context => complete(c)
                  case _ => complete(serverErrorResponse(errorType = "DeleteError", errorMessage = "failed to delete context"))
                }

            }

          }
        }

      }
    }

  }

}
