package com.ubirch.user.core.manager

import com.ubirch.user.testTools.db.mongo.{DefaultModels, MongoSpec}

/**
  * author: cvandrei
  * since: 2017-04-07
  */
class GroupsManagerSpec extends MongoSpec {

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

    // TODO remaining tests

  }

}
