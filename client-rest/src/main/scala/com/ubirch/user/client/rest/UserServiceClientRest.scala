package com.ubirch.user.client.rest

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.client.rest.config.UserClientRestConfig
import com.ubirch.user.model.rest.{Group, User}
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonResponse

import org.json4s.native.Serialization.read

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.stream.Materializer
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-05-15
  */
object UserServiceClientRest extends MyJsonProtocol
  with StrictLogging {

  def check()(implicit httpClient: HttpExt, materializer: Materializer): Future[Option[JsonResponse]] = {


    val url = UserClientRestConfig.urlCheck
    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[JsonResponse](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        Future(
          logErrorAndReturnNone(s"check() call to key-service failed: url=$url code=$code, status=${res.status}")
        )

    }

  }

  def deepCheck()(implicit httpClient: HttpExt, materializer: Materializer): Future[DeepCheckResponse] = {

    val statusCodes: Set[StatusCode] = Set(StatusCodes.OK, StatusCodes.ServiceUnavailable)

    val url = UserClientRestConfig.urlDeepCheck

    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(status, _, entity, _) if statusCodes.contains(status) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          read[DeepCheckResponse](body.utf8String)
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        val errorText = s"deepCheck() call to user-service failed: url=$url code=$code, status=${res.status}"
        logger.error(errorText)
        val deepCheckRes = DeepCheckResponse(status = false, messages = Seq(errorText))
        Future(
          DeepCheckResponseUtil.addServicePrefix("user-service", deepCheckRes)
        )

    }

  }

  def groupMemberOf(contextName: String,
                    providerId: String,
                    externalUserId: String
                   )
                   (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[Set[Group]]] = {

    logger.debug("groups(): query groups through REST API")
    val url = UserClientRestConfig.pathGroupMemberOf(
      contextName = contextName,
      providerId = providerId,
      externalUserId = externalUserId
    )

    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[Set[Group]](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        Future(
          logErrorAndReturnNone(s"groups() call to user-service REST API failed: url=$url, code=$code")
        )

    }

  }

  def userGET(providerId: String,
              externalUserId: String
             )(implicit httpClient: HttpExt, materializer: Materializer): Future[Option[User]] = {

    logger.debug("userGET(): query user through REST API")
    val url = UserClientRestConfig.pathUserGET(
      providerId = providerId,
      externalUserId = externalUserId
    )

    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[User](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        Future(
          logErrorAndReturnNone(s"userGET() call to user-service REST API failed: url=$url, code=$code")
        )

    }

  }

  def userPOST(user: User)
              (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[User]] = {

    Json4sUtil.any2String(user) match {

      case Some(userJsonString: String) =>

        logger.debug(s"user (object): $userJsonString")
        val url = UserClientRestConfig.pathUserPOST()
        val req = HttpRequest(
          method = HttpMethods.POST,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(userJsonString))
        )
        httpClient.singleRequest(req) flatMap {

          case HttpResponse(StatusCodes.OK, _, entity, _) =>

            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Some(read[User](body.utf8String))
            }

          case res@HttpResponse(code, _, _, _) =>

            res.discardEntityBytes()
            Future(
              logErrorAndReturnNone(s"userPOST() call to user-service failed: url=$url code=$code, status=${res.status}")
            )

        }

      case None =>
        logger.error(s"failed to to convert input to JSON: user=$user")
        Future(None)

    }

  }

  def userPUT(user: User)
             (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[User]] = {

    Json4sUtil.any2String(user) match {

      case Some(userJsonString: String) =>

        logger.debug(s"user (object): $userJsonString")
        val url = UserClientRestConfig.pathUserPOST()
        val req = HttpRequest(
          method = HttpMethods.PUT,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(userJsonString))
        )
        httpClient.singleRequest(req) flatMap {

          case HttpResponse(StatusCodes.OK, _, entity, _) =>

            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Some(read[User](body.utf8String))
            }

          case res@HttpResponse(code, _, _, _) =>

            res.discardEntityBytes()
            Future(
              logErrorAndReturnNone(s"userPOST() call to user-service failed: url=$url code=$code, status=${res.status}")
            )

        }

      case None =>
        logger.error(s"failed to to convert input to JSON: user=$user")
        Future(None)

    }

  }

  def userDELETE(providerId: String,
                 externalUserId: String
                )(implicit httpClient: HttpExt, materializer: Materializer): Future[Boolean] = {

    logger.debug("userDELETE(): delete user through REST API")
    val url = UserClientRestConfig.pathUserDELETE(
      providerId = providerId,
      externalUserId = externalUserId
    )

    httpClient.singleRequest(HttpRequest(uri = url, method = HttpMethods.DELETE)) flatMap {

      case res@HttpResponse(StatusCodes.OK, _, _, _) =>

        res.discardEntityBytes()
        Future(true)

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        logErrorAndReturnNone(s"userGET() call to user-service REST API failed: url=$url, code=$code")
        Future(false)

    }

  }

  def emailExistsGET(email: String)
                    (implicit httpClient: HttpExt, materializer: Materializer): Future[Boolean] = {

    logger.debug("emailExistsGET(): search email address through REST API")
    val url = UserClientRestConfig.pathEmailExistsGET(email)

    httpClient.singleRequest(HttpRequest(uri = url)) map {

      case res@HttpResponse(StatusCodes.OK, _, entity, _) =>

        res.discardEntityBytes()
        true

      case res@HttpResponse(code, _, _, _) =>

        logErrorAndReturnNone(s"emailExistsGET() call to user-service REST API failed: url=$url, code=$code")
        false

    }

  }

  def hashedEmailExistsGET(hashedEmail: String)
                          (implicit httpClient: HttpExt, materializer: Materializer): Future[Boolean] = {

    logger.debug("emailExistsGET(): search email address through REST API")
    val url = UserClientRestConfig.pathHashedEmailExistsGET(hashedEmail)

    httpClient.singleRequest(HttpRequest(uri = url)) map {

      case res@HttpResponse(StatusCodes.OK, _, entity, _) =>

        res.discardEntityBytes()
        true

      case res@HttpResponse(code, _, _, _) =>

        logErrorAndReturnNone(s"emailExistsGET() call to user-service REST API failed: url=$url, code=$code")
        false

    }

  }

  private def logErrorAndReturnNone[T](errorMsg: String,
                                       t: Option[Throwable] = None
                                      ): Option[T] = {
    t match {
      case None => logger.error(errorMsg)
      case Some(someThrowable: Throwable) => logger.error(errorMsg, someThrowable)
    }

    None

  }

}
