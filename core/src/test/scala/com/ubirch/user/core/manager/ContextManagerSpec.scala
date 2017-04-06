package com.ubirch.user.core.manager

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Context
import com.ubirch.user.testTools.db.mongo.MongoSpec
import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-04-05
  */
class ContextManagerSpec extends MongoSpec {

  feature("create()") {

    scenario("context does NOT exist --> success") {

      // prepare
      val context = Context(displayName = "automated-test")

      // test
      ContextManager.create(context) flatMap { created =>

        // verify
        created shouldBe Some(context)
        mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 1)

      }

    }

    scenario("context with same id exists --> fail") {

      // prepare
      ContextManager.create(Context(displayName = "automated-test")) flatMap {

        case None => fail("failed during preparation")

        case Some(existingContext) =>

          val context = existingContext.copy(displayName = existingContext.displayName + "-foo")

          // test
          ContextManager.create(context) flatMap { created =>

            // verify
            created shouldBe None
            mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 1)

          }

      }

    }

    scenario("context with same name exists --> fail") {

      // prepare
      ContextManager.create(Context(displayName = "automated-test")) flatMap {

        case None => fail("failed during preparation")

        case Some(existingContext) =>

          val context = existingContext.copy(id = UUIDUtil.uuid)

          // test
          ContextManager.create(context) flatMap { created =>

            // verify
            created shouldBe None
            mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 1)

          }

      }

    }

  }

  // update()
  // TODO context.id does not exist --> fail
  // TODO context.id exists --> success

  // get()
  // TODO context.id does not exist --> fail
  // TODO context.id exists --> success

  // findByName()
  // TODO context.name does not exist --> fail
  // TODO context.name exists --> success

  // delete()
  // TODO context.id does not exist --> fail
  // TODO context.id exists --> success

}
