package com.ubirch.user.server.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.actor.{ActorNames, FindGroups, FoundGroups, GroupsActor}
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
trait GroupsRoute extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with StrictLogging {

  implicit val system = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout = Timeout(Config.actorTimeout seconds)

  private val groupsActor = system.actorOf(new RoundRobinPool(Config.akkaNumberOfWorkers).props(Props[GroupsActor]), ActorNames.GROUPS)

  val route: Route = {

    respondWithCORS {
      path(RouteConstants.groups / Segment / Segment) { (contextName, externalUserId) =>

        get {
          findByContextNameAndExternalUserId(contextName = contextName, externalUserId = externalUserId)
        }

      }
    }

  }

  private def findByContextNameAndExternalUserId(contextName: String, externalUserId: String): Route = {

    onComplete(groupsActor ? FindGroups(contextName, externalUserId)) {

      case Failure(t) =>
        logger.error("findGroups call responded with an unhandled message (check GroupsRoute for bugs!!!)")
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>
        resp match {
          case found: FoundGroups => complete(found.groups)
          case _ => complete(serverErrorResponse(errorType = "QueryError", errorMessage = "failed to query groups"))
        }

    }

  }

}
