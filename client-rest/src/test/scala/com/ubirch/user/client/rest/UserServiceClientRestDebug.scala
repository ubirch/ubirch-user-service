package com.ubirch.user.client.rest

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.rest.{Group, User}

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
  val contextName = "ubirch-dev"
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
    emailExistsGET("test@ubirch.com")
    emailExistsGET("testtest@ubirch.com")

    // DELETE /user/$PROVIDER/$EXTERNAL_USER_ID
    userDELETE(providerId, externalUserId)
    userDELETE(providerId, externalUserId)

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

  private def emailExistsGET(email: String): Unit = {

    val result = Await.result(UserServiceClientRest.emailExistsGET(email), 5 seconds)
    logger.info(s"___ emailExistsGET($email): $result")

  }

}
