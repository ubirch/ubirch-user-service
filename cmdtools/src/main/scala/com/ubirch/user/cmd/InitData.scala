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

  val contextModel = DefaultModels.context()
  val ownerModel = DefaultModels.user(displayName = "test-user-1", externalId = "1234")
  val user2Model = DefaultModels.user(displayName = "test-user-2", externalId = "1235")
  val user3Model = DefaultModels.user(displayName = "test-user-3", externalId = "1236")

  val foo = for {

    contextOpt <- ContextManager.create(contextModel)
    ownerOpt <- UserManager.create(ownerModel)
    user2Opt <- UserManager.create(user2Model)
    user3Opt <- UserManager.create(user3Model)
    groupOpt <- dataHelpers.createGroup(contextOpt, ownerOpt, user2Opt)

  } yield {

    mongo.close()
    CreatedData(contextOpt.get, ownerOpt.get, groupOpt.get, user2Opt.get, user3Opt.get)

  }

  foo map { created =>

    logger.info(s"=== created context: name=${created.context.displayName}")

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
