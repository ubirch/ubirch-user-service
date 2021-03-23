package com.ubirch.user.cmd

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.{Config, ConfigKeys}
import com.ubirch.user.core.manager.ContextManager
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.user.testTools.external.DataHelpers
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * author: cvandrei
  * since: 2017-04-07
  */
object InitUsers extends App
  with StrictLogging {

  private implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  private val dataHelpers = new DataHelpers

  // TODO migrate to encapsulate all executable logic within a method `run(): Unit`
  val dataCreated = for {

    contextOpt <- ContextManager.findByName(Config.testUserContext)

    user1Opt <- dataHelpers.createUser(displayName = "test-user-1", externalId = "1234")
    user2Opt <- dataHelpers.createUser(displayName = "test-user-2", externalId = "1235")
    user3Opt <- dataHelpers.createUser(displayName = "test-user-3", externalId = "1236")
    group1Opt <- dataHelpers.createGroup(contextOpt, user1Opt, adminGroup = None, user2Opt)
    group2Opt <- dataHelpers.createGroup(contextOpt, user2Opt, adminGroup = None)
    group3Opt <- dataHelpers.createGroup(contextOpt, user3Opt, adminGroup = None)

  } yield {

    mongo.close()

    if (contextOpt.isEmpty) {
      logger.error(s"=== context is missing (did you call the /initData endpoint?): name=${contextOpt.get.displayName}")
    }

    val ctx = contextOpt.get
    Seq(
      CreatedData(ctx, user1Opt.get, group1Opt.get, user2Opt.get, user3Opt.get),
      CreatedData(ctx, user2Opt.get, group2Opt.get),
      CreatedData(ctx, user3Opt.get, group3Opt.get)
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
