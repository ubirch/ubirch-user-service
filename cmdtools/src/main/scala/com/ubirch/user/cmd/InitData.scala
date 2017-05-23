package com.ubirch.user.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.ConfigKeys
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.user.testTools.external.DataHelpers
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-04-07
  */
object InitData extends App
  with StrictLogging {

  private implicit val mongo = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  private val dataHelpers = new DataHelpers

  val dataCreated = for {

    // context: *-local
    contextUbirchLocalOpt <- dataHelpers.createContext(displayName = "ubirch-local")
    contextTrackleLocalOpt <- dataHelpers.createContext(displayName = "trackle-local")
    contextUbirchAdminUiLocalOpt <- dataHelpers.createContext(displayName = "ubirch-admin-ui-local")
    contextTrackleAdminUiLocalOpt <- dataHelpers.createContext(displayName = "trackle-admin-ui-local")
    localContexts = Set(contextUbirchLocalOpt, contextTrackleLocalOpt, contextUbirchAdminUiLocalOpt, contextTrackleAdminUiLocalOpt)

    // context: *-dev
    contextUbirchDevOpt <- dataHelpers.createContext(displayName = "ubirch-dev")
    contextTrackleDevOpt <- dataHelpers.createContext(displayName = "trackle-dev")
    contextUbirchAdminUiDevOpt <- dataHelpers.createContext(displayName = "ubirch-admin-ui-dev")
    contextTrackleAdminUiDevOpt <- dataHelpers.createContext(displayName = "trackle-admin-ui-dev")
    devContexts = Set(contextUbirchDevOpt, contextTrackleDevOpt, contextUbirchAdminUiDevOpt, contextTrackleAdminUiDevOpt)

    // context: *-demo
    contextUbirchAdminUiDemoOpt <- dataHelpers.createContext(displayName = "ubirch-admin-ui-demo")
    demoContexts = Set(contextUbirchAdminUiDemoOpt)

    allContexts = localContexts ++ devContexts ++ demoContexts

    user1Opt <- dataHelpers.createUser(displayName = "test-user-1", externalId = "1234")
    user2Opt <- dataHelpers.createUser(displayName = "test-user-2", externalId = "1235")
    user3Opt <- dataHelpers.createUser(displayName = "test-user-3", externalId = "1236")
    group1Opt <- dataHelpers.createGroup(contextUbirchDevOpt, user1Opt, user2Opt)
    group2Opt <- dataHelpers.createGroup(contextUbirchDevOpt, user2Opt)
    group3Opt <- dataHelpers.createGroup(contextUbirchDevOpt, user3Opt)

  } yield {

    mongo.close()

    for (context <- allContexts) {
      if (context.isDefined) {
        logger.info(s"=== created context: name=${context.get.displayName}")
      } else {
        logger.error(s"=== failed to create context: name=${context.get.displayName}")
      }
    }

    Seq(
      CreatedData(contextUbirchDevOpt.get, user1Opt.get, group1Opt.get, user2Opt.get, user3Opt.get),
      CreatedData(contextUbirchDevOpt.get, user2Opt.get, group2Opt.get),
      CreatedData(contextUbirchDevOpt.get, user3Opt.get, group3Opt.get)
    )

  }

  dataCreated map { createdList =>

    createdList foreach { created =>

      val owner = created.owner
      val ownerString = s"id=${owner.id}; provider=${owner.providerId}; externalId=${owner.externalId}"
      logger.info(s"=== created user (owner): $ownerString")

      for (user <- created.allowedUsers) {
        val userString = s"id=${user.id}; provider=${user.providerId}; externalId=${user.externalId}"
        logger.info(s"=== created user (allowedUser): $userString")
      }

      logger.info(s"=== created group: id=${created.group.id}; id=${created.group.displayName}")

    }

  }

}

case class CreatedData(context: Context, owner: User, group: Group, allowedUsers: User*)
