package com.ubirch.user.client.rest

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.util.ByteString
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.user.client.rest.config.UserClientRestConfig
import com.ubirch.user.model.rest._
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.json.{Json4sUtil, MyJsonProtocol}
import com.ubirch.util.model.JsonResponse
import org.json4s.native.Serialization.read

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
        val url = UserClientRestConfig.pathUserPOST
        postUserHelperMethod(url, userJsonString)

      case None =>
        logger.error(s"failed to to convert input to JSON: user=$user")
        Future(None)

    }
  }

  def userRecreate(user: User)
                  (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[User]] = {

    Json4sUtil.any2String(user) match {

      case Some(userJsonString: String) =>

        logger.debug(s"recreate: $userJsonString")
        val url = UserClientRestConfig.pathUserRecreate
        postUserHelperMethod(url, userJsonString)

      case None =>
        logger.error(s"failed to to convert input to JSON: user=$user")
        Future(None)

    }
  }


  private def postUserHelperMethod(url: Uri, userJsonString: String)
                                  (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[User]] = {
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
  }

  def userPUT(user: User)
             (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[User]] = {

    Json4sUtil.any2String(user) match {

      case Some(userJsonString: String) =>

        logger.debug(s"user (object): $userJsonString")
        val url = UserClientRestConfig.pathUserPUT(
          providerId = user.providerId,
          externalUserId = user.externalId
        )
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

  def extIdExistsGET(externalId: String)
                    (implicit httpClient: HttpExt, materializer: Materializer): Future[Boolean] = {

    logger.debug("extIdExistsGET(): search external ID through REST API")
    val url = UserClientRestConfig.pathExternalIdExistsGET(externalId)

    httpClient.singleRequest(HttpRequest(uri = url)) map {

      case res@HttpResponse(StatusCodes.OK, _, entity, _) =>

        res.discardEntityBytes()
        true

      case res@HttpResponse(code, _, _, _) =>

        logErrorAndReturnNone(s"emailExistsGET() call to user-service REST API failed: url=$url, code=$code")
        false

    }

  }

  def registerPOST(userContext: UserContext)
                  (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[UserInfo]] = {

    Json4sUtil.any2String(userContext) match {

      case Some(userContextJsonString: String) =>

        logger.debug(s"userContext (object): $userContextJsonString")
        val url = UserClientRestConfig.pathRegisterPOST
        val req = HttpRequest(
          method = HttpMethods.POST,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(userContextJsonString))
        )
        httpClient.singleRequest(req) flatMap {

          case HttpResponse(StatusCodes.OK, _, entity, _) =>

            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Some(read[UserInfo](body.utf8String))
            }

          case res@HttpResponse(code, _, _, _) =>

            res.discardEntityBytes()
            Future(
              logErrorAndReturnNone(s"registerPOST() call to user-service failed: url=$url code=$code, status=${res.status}, userContextJson=$userContextJsonString")
            )

        }

      case None =>
        logger.error(s"failed to to convert input to JSON: userContext=$userContext")
        Future(None)

    }

  }

  def userInfoGET(context: String,
                  providerId: String,
                  externalUserId: String
                 )
                 (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[UserInfo]] = {

    logger.debug(s"userInfoGET(): search userInfo ($context/$providerId/$externalUserId) through REST API")
    val url = UserClientRestConfig.pathUserInfoGET(context = context, providerId = providerId, externalUserId = externalUserId)

    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[UserInfo](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        logErrorAndReturnNone(s"userInfoGET() call to user-service REST API failed: url=$url, code=$code")
        Future(None)

    }

  }

  def userInfoPUT(updateInfo: UpdateInfo)
                 (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[UserInfo]] = {

    Json4sUtil.any2String(updateInfo) match {

      case Some(updateInfoJsonString: String) =>

        logger.debug(s"updateInfo (object): $updateInfoJsonString")
        val url = UserClientRestConfig.pathUserInfoPUT
        val req = HttpRequest(
          method = HttpMethods.PUT,
          uri = url,
          entity = HttpEntity.Strict(ContentTypes.`application/json`, data = ByteString(updateInfoJsonString))
        )
        httpClient.singleRequest(req) flatMap {

          case HttpResponse(StatusCodes.OK, _, entity, _) =>

            entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
              Some(read[UserInfo](body.utf8String))
            }

          case res@HttpResponse(code, _, _, _) =>

            res.discardEntityBytes()
            Future(
              logErrorAndReturnNone(s"userInfoPUT() call to user-service failed: url=$url code=$code, status=${res.status}")
            )

        }

      case None =>
        logger.error(s"failed to to convert input to JSON: updateInfo=$updateInfo")
        Future(None)

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
