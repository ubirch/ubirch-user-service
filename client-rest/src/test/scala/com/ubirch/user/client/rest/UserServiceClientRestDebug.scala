package com.ubirch.user.client.rest

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.rest.{Group, SimpleUserContext, UpdateInfo, User, UserContext, UserInfo, UserUpdate}

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2017-05-16
  */
object UserServiceClientRestDebug extends App
  with StrictLogging {

  implicit val system: ActorSystem = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  implicit private val httpClient: HttpExt = Http()

  // contextName, providerId and externalUserId have been created by InitData
  val contextName = "ubirch-local"
  val providerId = "google"
  val externalUserId = "1234"

  try {

    // GET /group/memberOf/$CONTEXT_NAME/$PROVIDER_ID/$EXTERNAL_USER_ID
    val futureGroups = UserServiceClientRest.groupMemberOf(
      contextName = contextName,
      providerId = providerId,
      externalUserId = externalUserId
    )
    val groupsOpt = Await.result(futureGroups, 5 seconds)

    groupsOpt match {

      case None => logger.info("====== groups found: None")

      case Some(groups: Set[Group]) =>
        logger.info(s"====== groups.size=${groups.size}")
        groups foreach { g =>
          logger.info(s"====== group=$g")
        }

    }

    // GET /user/$PROVIDER/$EXTERNAL_USER_ID
    userGET(providerId, externalUserId)
    userGET(providerId, externalUserId + "1")

    // GET /check
    val checkResponse = Await.result(UserServiceClientRest.check(), 20 seconds)
    logger.info(s"___ check(): $checkResponse")

    // GET /deepCheck
    val deepCheckResponse = Await.result(UserServiceClientRest.deepCheck(), 20 seconds)
    logger.info(s"___ deepCheck(): $deepCheckResponse")

    // GET /user/emailExists/$EMAIL_ADDRESS
    externalIdExistsGET("test@ubirch.com")
    externalIdExistsGET("testtest@ubirch.com")

    // DELETE /user/$PROVIDER/$EXTERNAL_USER_ID
    userDELETE(providerId, externalUserId)
    userDELETE(providerId, externalUserId)

    // POST /register
    //   followed by /userInfo calls
    val userName = "some user name"
    val locale = "en-US"
    val email = Some("someUser@ubirch.com")
    registerPOST(contextName, providerId, externalUserId, userName, locale, email) match {

      case None => // no clean-up necessary since user hasn't been created

      case Some(_) =>

        userInfoGET(contextName, providerId, externalUserId) match {

          case None =>

          case Some(_) =>

            val newDisplayName = userName + "--new"
            userInfoPUT(contextName, providerId, externalUserId, newDisplayName)
            userInfoGET(contextName, providerId, externalUserId)

        }
        userDELETE(providerId, externalUserId) // (clean-up) delete user again

    }

  } finally {
    system.terminate()
  }

  private def userGET(providerId: String, externalUserId: String): Unit = {

    val futureUser = UserServiceClientRest.userGET(
      providerId = providerId,
      externalUserId = externalUserId
    )
    Await.result(futureUser, 5 seconds) match {
      case None => logger.info(s"___ userGET($providerId, $externalUserId): None")
      case Some(user: User) => logger.info(s"___ userGET($providerId, $externalUserId): $user")
    }

  }

  private def userDELETE(providerId: String, externalUserId: String): Unit = {

    val futureUser = UserServiceClientRest.userDELETE(
      providerId = providerId,
      externalUserId = externalUserId
    )
    Await.result(futureUser, 5 seconds) match {
      case false => logger.info(s"___ userDELETE($providerId, $externalUserId): false")
      case true => logger.info(s"___ userDELETE($providerId, $externalUserId) true")
    }

  }

  private def externalIdExistsGET(externalId: String): Unit = {

    val result = Await.result(UserServiceClientRest.extIdExistsGET(externalId), 5 seconds)
    logger.info(s"___ emailExistsGET($externalId): $result")

  }

  private def registerPOST(context: String,
                           providerId: String,
                           userId: String,
                           userName: String,
                           locale: String,
                           email: Option[String]
                          ): Option[UserInfo] = {


    val userContext = UserContext(
      context = context,
      providerId = providerId,
      userId = userId,
      userName = userName,
      locale = locale,
      email = email
    )

    val result = Await.result(UserServiceClientRest.registerPOST(userContext), 1000 seconds)
    logger.info(s"___ registerPOST($userContext): $result")

    result

  }

  private def userInfoGET(context: String,
                          providerId: String,
                          userId: String
                         ): Option[UserInfo] = {

    val simpleUserContext = SimpleUserContext(
      context = context,
      providerId = providerId,
      userId = userId
    )

    val result = Await.result(UserServiceClientRest.userInfoGET(context, providerId, userId), 1000 seconds)
    logger.info(s"___ userInfoGET($simpleUserContext): $result")

    result

  }

  private def userInfoPUT(context: String,
                          providerId: String,
                          userId: String,
                          newDisplayName: String
                         ): Option[UserInfo] = {

    val updateInfo = UpdateInfo(
      SimpleUserContext(
        context = context,
        providerId = providerId,
        userId = userId
      ),
      UserUpdate(newDisplayName)
    )

    val result = Await.result(UserServiceClientRest.userInfoPUT(updateInfo), 1000 seconds)
    logger.info(s"___ userInfoPUT($updateInfo): $result")

    result

  }

}
