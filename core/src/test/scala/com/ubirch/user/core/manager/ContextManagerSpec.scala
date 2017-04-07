package com.ubirch.user.core.manager

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.tools.DefaultModels
import com.ubirch.user.testTools.db.mongo.MongoSpec
import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-04-05
  */
class ContextManagerSpec extends MongoSpec {

  private val collection: String = Config.mongoCollectionContext
  
  feature("create()") {

    scenario("context does NOT exist --> success") {

      // prepare
      val context = DefaultModels.context()

      // test
      ContextManager.create(context) flatMap { created =>

        // verify
        created shouldBe Some(context)
        Thread.sleep(200)
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)

      }

    }

    scenario("context with same id exists --> fail") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(existingContext) =>

          val context = existingContext.copy(displayName = existingContext.displayName + "-foo")

          // test
          ContextManager.create(context) flatMap { created =>

            // verify
            created shouldBe None
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    scenario("context with same name exists --> fail") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(existingContext) =>

          val context = existingContext.copy(id = UUIDUtil.uuid)

          // test
          ContextManager.create(context) flatMap { created =>

            // verify
            created shouldBe None
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("update()") {

    scenario("context.id does not exist --> fail") {

      // test
      ContextManager.update(DefaultModels.context()) flatMap { updated =>

        // verify
        updated shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    scenario("context.id exists --> success") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          val update = context.copy(displayName = s"${context.displayName}-test")

          // test
          ContextManager.update(update) flatMap { result =>

            // verify
            result shouldBe Some(update)
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("findById()") {

    scenario("context.id does not exist --> fail") {

      // test
      ContextManager.findById(UUIDUtil.uuid) flatMap { created =>

        // verify
        created shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    scenario("context.id exists --> success") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          // test
          ContextManager.findById(context.id) flatMap { result =>

            // verify
            result shouldBe Some(context)
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

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
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    scenario("context.name exists --> success") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          // test
          ContextManager.findByName(context.displayName) flatMap { result =>

            // verify
            result shouldBe Some(context)
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("delete()") {

    scenario("context.id does not exist --> fail") {

      // test
      ContextManager.delete(UUIDUtil.uuid) flatMap { result =>

        // verify
        result shouldBe false
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    scenario("context.id exists --> success") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          // test
          ContextManager.delete(context.id) flatMap { result =>

            // verify
            result shouldBe true
            mongoTestUtils.countAll(collection) map (_ shouldBe 0)

          }

      }

    }

  }

}
