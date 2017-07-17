package com.ubirch.user.core.manager

import com.ubirch.user.core.manager.testUtils.DataHelpers
import com.ubirch.user.model.db.tools.DefaultModels
import com.ubirch.user.testTools.db.mongo.MongoSpec

/**
  * author: cvandrei
  * since: 2017-04-07
  */
class GroupsManagerSpec extends MongoSpec {

  private val dataHelpers = new DataHelpers

  feature("findByContextAndUser") {

    scenario("empty database") {

      // prepare
      val context = DefaultModels.context()
      val user = DefaultModels.user()

      // test
      GroupsManager.findByContextAndUser(
        contextName = context.displayName,
        providerId = user.providerId,
        externalUserId = user.externalId
      ) map { groups =>

        // verify
        groups should be('isEmpty)

      }

    }

    scenario("user is group owner") {

      // prepare
      val contextModel = DefaultModels.context()
      val ownerModel = DefaultModels.user(displayName = "test-user-1", externalId = "1234")
      val user2Model = DefaultModels.user(displayName = "test-user-2", externalId = "1235")

      for {
        contextOpt <- ContextManager.create(contextModel)
        ownerOpt <- UserManager.create(ownerModel)
        user2Opt <- UserManager.create(user2Model)
        groupOpt <- dataHelpers.createGroup(contextOpt, ownerOpt, user2Opt)

        // test
        result <- dataHelpers.findGroup(contextOpt.get, ownerOpt.get)

      } yield {
        // verify
        result shouldBe Set(groupOpt.get)
      }

    }

    scenario("user is allowed to access the group") {

      // prepare
      val contextModel = DefaultModels.context()
      val ownerModel = DefaultModels.user(displayName = "test-user-1", externalId = "1234")
      val user2Model = DefaultModels.user(displayName = "test-user-2", externalId = "1235")

      for {
        contextOpt <- ContextManager.create(contextModel)
        ownerOpt <- UserManager.create(ownerModel)
        user2Opt <- UserManager.create(user2Model)
        groupOpt <- dataHelpers.createGroup(contextOpt, ownerOpt, user2Opt)

        // test
        result <- dataHelpers.findGroup(contextOpt.get, user2Opt.get)

      } yield {
        // verify
        result shouldBe Set.empty
      }

    }

    scenario("user owns no groups nor is allowed to access any of them") {

      // prepare
      val contextModel = DefaultModels.context()
      val ownerModel = DefaultModels.user(displayName = "test-user-1", externalId = "1234")
      val user2Model = DefaultModels.user(displayName = "test-user-2", externalId = "1235")
      val user3Model = DefaultModels.user(displayName = "test-user-3", externalId = "1236")

      for {
        contextOpt <- ContextManager.create(contextModel)
        ownerOpt <- UserManager.create(ownerModel)
        user2Opt <- UserManager.create(user2Model)
        user3Opt <- UserManager.create(user3Model)
        groupOpt <- dataHelpers.createGroup(contextOpt, ownerOpt, user2Opt)

        // test
        result <- dataHelpers.findGroup(contextOpt.get, user3Opt.get)

      } yield {
        // verify
        result shouldBe Set.empty
      }

    }

  }

}
