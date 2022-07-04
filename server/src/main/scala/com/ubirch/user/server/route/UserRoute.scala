package com.ubirch.user.server.route

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.core.actor._
import com.ubirch.user.model._
import com.ubirch.user.model.rest._
import com.ubirch.user.server.formats.UserFormats
import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.http.response.ResponseUtil
import com.ubirch.util.json.JsonFormats
import com.ubirch.util.model.{JsonErrorResponse, JsonResponse}
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.rest.akka.directives.CORSDirective
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import org.json4s.Formats
import org.json4s.native.Serialization.{read, write}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

/**
  * author: cvandrei
  * since: 2017-03-30
  */
class UserRoute(implicit mongo: MongoUtil, val system: ActorSystem) extends CORSDirective
  with ResponseUtil
  with WithRoutesHelpers
  with StrictLogging {

  implicit val formats: Formats = JsonFormats.default ++ UserFormats.all

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = Timeout(Config.actorTimeout seconds)

  private val userActor = system.actorOf(UserActor.props(), ActorNames.USER)
  private val userInfoActor = system.actorOf(UserInfoActor.props(), ActorNames.USER_INFO)

  val route: Route = {
    pathPrefix(RouteConstants.users) {
      respondWithCORS {
        pathEnd {
          get {
            parameters("limit".as[Int], "lastCreatedAt".optional) { (limit: Int, lastCreatedAt: Option[String]) =>
              getUsersWithPagination(limit, lastCreatedAt)
            }
          }
        }
      }
    } ~ pathPrefix(RouteConstants.user) {
      respondWithCORS {

        pathEnd {

          post {
            entity(as[User]) { user =>
              createUser(user)
            }
          }

        } ~ path(RouteConstants.recreate) {

          post {
            entity(as[User]) { user =>
              restoreUser(user)
            }
          }

        } ~ path(RouteConstants.externalIdExists / Segment) { externalId =>

          get {
            searchByExternalId(externalId)
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

        } ~ pathPrefix(RouteConstants.info) {

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

              put {
                entity(as[UpdateInfo]) { updateInfo =>
                  update(updateInfo)
                }
              }

            }
          }

        } ~ pathPrefix(RouteConstants.activation) {
          post {
            entity(as[ActivationUpdate]) { users =>
              updateActivation(users)
            }
          }
        }

      }
    }
  }

  private def createUser(restUser: User): Route = {

    val dbUser = read[db.User](write(restUser))

    OnComplete(userActor ? CreateUser(dbUser)).fold() {

      case Success(resp) =>

        resp match {

          case Some(u: db.User) =>
            complete(StatusCodes.OK -> read[rest.User](write(u)))

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


  private def restoreUser(restUser: User): Route = {

    val dbUser = read[db.User](write(restUser))

    OnComplete(userActor ? RestoreUser(dbUser)).fold() {

      case Success(resp) =>

        resp match {

          case Some(u: db.User) =>
            complete(StatusCodes.OK -> read[rest.User](write(u)))

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

    val dbUser = read[db.User](write(restUser))

    OnComplete(userActor ? UpdateUser(
      providerId = providerId,
      externalUserId = externalUserId,
      dbUser)
    ).fold() {

      case Success(resp) =>
        resp match {

          case Some(u: db.User) =>
            logger.debug("successfully updated user")
            complete(StatusCodes.OK -> read[rest.User](write(u)))

          case jer: JsonErrorResponse =>
            logger.debug(s"failed to update user $jer")
            complete(StatusCodes.BadRequest -> jer)

          case _ =>
            logger.debug(s"failed to update user, serverErrorResponse")
            complete(serverErrorResponse(errorType = "UpdateError", errorMessage = "failed to update user"))

        }

      case Failure(t) =>
        logger.error("update user call responded with an unhandled message (check UserRoute for bugs!!!)", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

    }

  }

  private def findByProviderUserId(providerId: String, externalUserId: String): Route = {

    OnComplete(userActor ? FindUser(providerId, externalUserId)).fold() {

      case Failure(t) =>

        logger.error("UserRoute.findByProviderUserId() -- ended with Failure", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = "sorry, something went wrong on our end"))

      case Success(resp) =>

        resp match {

          case None =>

            val jsonError = JsonErrorResponse(errorType = "QueryError", errorMessage = "failed to find user")
            logger.debug(s"UserRoute.findByProviderUserId() -- userFound=None (providerId=$providerId, externalUserId=$externalUserId)")
            complete(requestErrorResponse(jsonError))

          case Some(u: db.User) =>

            logger.debug(s"UserRoute.findByProviderUserId() -- userFound=Some (providerId=$providerId, externalUserId=$externalUserId)")
            complete(read[rest.User](write(u)))

          case _ =>

            logger.error(s"UserRoute.findByProviderUserId() -- unhandled Success response (providerId=$providerId, externalUserId=$externalUserId)")
            complete(serverErrorResponse(errorType = "QueryError", errorMessage = "failed to query user"))

        }

    }

  }

  private def deleteById(providerId: String, externalUserId: String): Route = {

    OnComplete(userActor ? DeleteUser(providerId = providerId, externalUserId = externalUserId)).fold() {

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

  private def searchByExternalId(externalId: String): Route = {

    OnComplete(userActor ? SearchByExternalId(externalId)).fold() {

      case Failure(t) =>
        logger.error("searchByExternalId call responded with an unhandled message", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))

      case Success(resp) =>
        resp match {
          case true =>
            complete(StatusCodes.OK -> JsonResponse(message = s"external ID exist: $externalId"))
          case false =>
            complete(StatusCodes.BadRequest -> JsonResponse(message = s"external ID  does not exist: $externalId"))
          case jre: JsonErrorResponse =>
            complete(StatusCodes.BadRequest -> jre)
          case _ =>
            val errMsg = s"no user with given external ID exists: $externalId"
            logger.error(errMsg)
            complete(StatusCodes.BadRequest -> JsonErrorResponse(errorType = "QueryError", errorMessage = errMsg))
        }

    }

  }

  private def getInfo(simpleUserContext: SimpleUserContext): Route = {

    OnComplete(userInfoActor ? simpleUserContext).fold() {

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

    OnComplete(userInfoActor ? updateInfo).fold() {

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

  private def updateActivation(updates: ActivationUpdate): Route = {

    OnComplete(userActor ? updates).fold() {

      case Failure(t) =>
        logger.error("Activation Update failed", t)
        complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))

      case Success(either) =>
        either match {
          case Right(msg: String) => complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`text/csv(UTF-8)`, msg)))

          case Left(msg: String) => complete(HttpResponse(BadRequest, entity = HttpEntity(ContentTypes.`text/csv(UTF-8)`, msg)))

          case jsonError: JsonErrorResponse => complete(serverErrorResponse(jsonError))
        }
    }
  }

  /**
   * Response users created after lastCreatedAtOpt
   * @param limit: maximum number of users
   * @param lastCreatedAtOpt: DateTime string
   */
  private def getUsersWithPagination(limit: Int, lastCreatedAtOpt: Option[String]): Route = {
    Try(lastCreatedAtOpt.map { dateTimeStr =>
      DateTime.parse(dateTimeStr)
    }) match {
      case Failure(ex) =>
        logger.info("lastCreatedAt has a wrong date format", ex.getMessage)
        complete(requestErrorResponse(errorType = "BadRequest", errorMessage = "lastCreatedAt must be datetime"))
      case Success(lastCreatedAtOpt) =>
        OnComplete(userActor ? GetUsersWithPagination(limit, lastCreatedAtOpt)).fold() {
          case Failure(t) =>
            logger.error("getUsersWithPagination", t)
            complete(serverErrorResponse(errorType = "ServerError", errorMessage = t.getMessage))

          case Success(resp) =>
            resp match {
              case userList: List[db.User] =>
                complete(read[List[rest.User]](write(userList)))
              case _ =>
                complete(serverErrorResponse(errorType = "QueryError", errorMessage = "failed to query users"))
            }

        }
    }
  }

}
