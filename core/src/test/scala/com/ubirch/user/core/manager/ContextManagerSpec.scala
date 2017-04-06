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

  feature("findById()") {

    scenario("context.id does not exist --> fail") {

      // test
      ContextManager.findById(UUIDUtil.uuid) flatMap { created =>

        // verify
        created shouldBe None
        mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 0)

      }

    }

    scenario("context.id exists --> success") {

      // prepare
      ContextManager.create(Context(displayName = "automated-test")) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          // test
          ContextManager.findById(context.id) flatMap { result =>

            // verify
            result shouldBe Some(context)
            mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("findByName()") {

    scenario("context.name does not exist --> fail") {

      // test
      ContextManager.findByName("automated-test") flatMap { created =>

        // verify
        created shouldBe None
        mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 0)

      }

    }

    scenario("context.name exists --> success") {

      // prepare
      ContextManager.create(Context(displayName = "automated-test")) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          // test
          ContextManager.findByName(context.displayName) flatMap { result =>

            // verify
            result shouldBe Some(context)
            mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("delete()") {

    scenario("context.id does not exist --> fail") {

      // test
      ContextManager.delete(UUIDUtil.uuid) flatMap { created =>

        // verify
        created shouldBe false
        mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 0)

      }

    }

    scenario("context.id exists --> success") {

      // prepare
      ContextManager.create(Context(displayName = "automated-test")) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          // test
          ContextManager.delete(context.id) flatMap { result =>

            // verify
            result shouldBe true
            mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 0)

          }

      }

    }

  }

}
