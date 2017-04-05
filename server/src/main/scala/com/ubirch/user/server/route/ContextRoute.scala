package com.ubirch.user.server.route

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.actor.{ActorNames, ContextActor, CreateContext, DeleteContext, FindContextByName, GetContext, UpdateContext}
import com.ubirch.user.model._
import com.ubirch.user.model.rest.Context
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
  * since: 2017-03-29
  */
class ContextRoute(implicit mongo: MongoUtil) extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val contextActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props(new ContextActor())), ActorNames.CONTEXT)

  val route: Route = {

    pathPrefix(RouteConstants.context) {
      respondWithCORS {

        pathEnd {

          post {
            entity(as[Context]) { context =>
              create(context)
            }
          } ~ put {
            entity(as[Context]) { context =>
              update(context)
            }
          }

        } ~ path(JavaUUID) { contextId =>

          get {
            getById(contextId)
          } ~ delete {
            deleteById(contextId)
          }

        } ~ path(RouteConstants.byName / Segment) { contextName =>

          get {
            findByName(contextName)
          }

        }

      }
    }

  }

  private def create(restContext: Context): Route = {

    val dbContext = Json4sUtil.any2any[db.Context](restContext)
    onComplete(contextActor ? CreateContext(dbContext)) {

      case Failure(t) =>
        logger.error("create context call responded with an unhandled message (check ContextRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "CreateError", errorMessage = "context already exists")
            complete(requestErrorResponse(jsonError))

          case Some(c: db.Context) => complete(Json4sUtil.any2any[rest.Context](c))

          case _ => complete(serverErrorResponse(errorType = "CreateError", errorMessage = "failed to create context"))

        }

    }

  }

  private def update(restContext: Context): Route = {

    val dbContext = Json4sUtil.any2any[db.Context](restContext)
    onComplete(contextActor ? UpdateContext(dbContext)) {

      case Failure(t) =>
        logger.error("update context call responded with an unhandled message (check ContextRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "UpdateError", errorMessage = "failed to update context")
            complete(requestErrorResponse(jsonError))

          case Some(c: db.Context) => complete(Json4sUtil.any2any[rest.Context](c))

          case _ => complete(serverErrorResponse(errorType = "UpdateError", errorMessage = "failed to update context"))

        }

    }

  }

  private def getById(id: UUID): Route = {

    onComplete(contextActor ? GetContext(id)) {

      case Failure(t) =>

        logger.error("getContext call responded with an unhandled message (check ContextRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>

        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "QueryError", errorMessage = "not found")
            complete(serverErrorResponse(response = jsonError, status = StatusCodes.NotFound))

          case Some(c: db.Context) => complete(Json4sUtil.any2any[rest.Context](c))

          case _ => complete(serverErrorResponse(errorType = "QueryError", errorMessage = "failed to query context"))

        }

    }

  }

  private def deleteById(id: UUID): Route = {

    onComplete(contextActor ? DeleteContext(id)) {

      case Failure(t) =>
        logger.error("deleteContext call responded with an unhandled message (check ContextRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {
          case deleted: Boolean if deleted => complete(StatusCodes.OK)
          case _ => complete(serverErrorResponse(errorType = "DeleteError", errorMessage = "failed to delete context"))
        }

    }

  }

  private def findByName(name: String): Route = {

    onComplete(contextActor ? FindContextByName(name)) {

      case Failure(t) =>

        logger.error("findContextByName call responded with an unhandled message (check ContextRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>

        resp match {

          case None =>
            val jsonError = JsonErrorResponse(errorType = "QueryError", errorMessage = "context not found")
            complete(requestErrorResponse(response = jsonError))

          case Some(c: db.Context) => complete(Json4sUtil.any2any[rest.Context](c))

          case _ => complete(serverErrorResponse(errorType = "QueryError", errorMessage = "failed to find context by name"))

        }

    }

  }

}
