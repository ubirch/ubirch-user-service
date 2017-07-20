package com.ubirch.user.client.rest

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.rest.Group

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

  implicit val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit val materializer = ActorMaterializer()

  implicit val httpClient: HttpExt = Http()

  // contextName, providerId and externalUserId have been created by InitData
  val contextName = "ubirch-dev"
  val providerId = "google"
  val externalUserId = "1234"

  try {

    val futureGroups = UserServiceClientRest.groups(
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

  } finally {
    system.terminate()
  }

}
