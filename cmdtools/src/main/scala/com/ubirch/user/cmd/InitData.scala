package com.ubirch.user.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.ConfigKeys
import com.ubirch.user.core.manager.{ContextManager, UserManager}
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.user.model.db.tools.DefaultModels
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

  val contextUbirchDev = DefaultModels.context(displayName = "ubirch-dev")
  val contextTrackleDev = DefaultModels.context(displayName = "trackle-dev")
  val ownerModel = DefaultModels.user(displayName = "test-user-1", externalId = "1234")
  val user2Model = DefaultModels.user(displayName = "test-user-2", externalId = "1235")
  val user3Model = DefaultModels.user(displayName = "test-user-3", externalId = "1236")

  val foo = for {

    contextUbirchOpt <- ContextManager.create(contextUbirchDev)
    contextTrackleOpt <- ContextManager.create(contextTrackleDev)
    ownerOpt <- UserManager.create(ownerModel)
    user2Opt <- UserManager.create(user2Model)
    user3Opt <- UserManager.create(user3Model)
    groupOpt <- dataHelpers.createGroup(contextUbirchOpt, ownerOpt, user2Opt)

  } yield {

    if (contextUbirchOpt.isDefined) {
      logger.info(s"=== created context: name=${contextUbirchOpt.get.displayName}")
    }
    if (contextTrackleOpt.isDefined) {
      logger.info(s"=== created context: name=${contextTrackleOpt.get.displayName}")
    }

    mongo.close()
    CreatedData(contextUbirchOpt.get, ownerOpt.get, groupOpt.get, user2Opt.get, user3Opt.get)

  }

  foo map { created =>

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

case class CreatedData(context: Context, owner: User, group: Group, allowedUsers: User*)
