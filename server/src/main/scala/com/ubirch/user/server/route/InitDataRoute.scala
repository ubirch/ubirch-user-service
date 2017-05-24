package com.ubirch.user.server.route

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.{ContextManager, GroupManager, UserManager}
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.futures.FutureUtil
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.MyJsonProtocol
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-05-24
  */
class InitDataRoute (implicit mongo: MongoUtil) extends MyJsonProtocol
  with CORSDirective
  with ResponseUtil
  with StrictLogging {

  val route: Route = {

    path(RouteConstants.initData / Segment) { environmentName =>
      respondWithCORS {

        get {
          onComplete(createContextsAndAdminUsers(environmentName)) {

            case Success(result) =>

              if (result) {
                complete(StatusCodes.OK)
              } else {
                complete(serverErrorResponse(errorType = "InitDataFailed", errorMessage = "failed to init data"))
              }

            case Failure(t) =>
              logger.error("failed to init data", t)
              complete(StatusCodes.InternalServerError)

          }
        }

      }
    }

  }

  private def createContextsAndAdminUsers(envName: String): Future[Boolean] = {

    for {
      Some(contextSeq) <- createContexts(envName)
      groups <- createAdminUsers(contextSeq)
    } yield {
      groups.isDefined
    }

  }

  private def createContexts(envName: String): Future[Option[Seq[Context]]] = {

    val futureResults: List[Future[Option[Context]]] = Config.contextPrefixList map { contextPrefix =>

      val contextName = s"$contextPrefix-$envName"
      ContextManager.findByName(contextName) flatMap {

        case None =>

          ContextManager.create(Context(displayName = contextName)) map {

            case None =>
              logger.error(s"initData: failed to created context: $contextName")
              None

            case Some(created: Context) =>
              logger.info(s"initData: created context: $contextName")
              Some(created)

          }

        case Some(existing: Context) =>
          logger.info(s"initData: context already exists: $contextName")
          Future(Some(existing))

      }

    }

    FutureUtil.unfoldInnerFutures(futureResults) map { results =>
      if (results.contains(None)) {
        None
      } else {
        Some(results.filter(_.isDefined).map(_.get))
      }
    }

  }

  private def createAdminUsers(contextSeq: Seq[Context]): Future[Option[Seq[Group]]] = {

    val futureGroups = contextSeq map { ctx =>

      for {

        Some(user) <- createUser()
        groupOpt <- createGroup(user, ctx)

      } yield {
        if (groupOpt.isEmpty) {
          logger.error(s"failed to create admin user and/or group: context=${ctx.displayName}")
        }
        groupOpt
      }

    }

    FutureUtil.unfoldInnerFutures(futureGroups) map { results =>

      if (results.contains(None)) {
        None
      } else {
        Some(results.filter(_.isDefined).map(_.get))
      }

    }

  }

  private def createUser(): Future[Option[User]] = {

    val providerId = Config.adminUserProviderId
    val externalUserId = Config.adminUserExternalId

    val user = User(
      displayName = "Admin",
      providerId = providerId,
      externalId = externalUserId,
      locale = "en"
    )

    UserManager.create(user)

  }

  private def createGroup(user: User, context: Context): Future[Option[Group]] = {

    val group = Group(
      displayName = "Admin Group",
      ownerId = user.id,
      contextId = context.id,
      allowedUsers = Set.empty
    )

    GroupManager.create(group)

  }

}
