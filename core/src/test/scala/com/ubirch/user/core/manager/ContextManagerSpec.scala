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
  
  Feature("create()") {

    Scenario("context does NOT exist --> success") {

      // prepare
      val context = DefaultModels.context()

      // test
      ContextManager.create(context) flatMap { created =>

        // verify
        created shouldBe Some(context)
        ContextManager.findById(created.get.id) map(_ should be(created))
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)
        ContextManager.delete(context.id).map(_ shouldBe true)
      }

    }

    Scenario("context with same id exists --> fail") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(existingContext) =>

          val context = existingContext.copy(displayName = existingContext.displayName + "-foo")

          // test
          ContextManager.create(context) flatMap { created =>

            // verify
            created shouldBe None
            ContextManager.findById(existingContext.id) map(_ should be(Some(existingContext)))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            ContextManager.delete(existingContext.id).map(_ shouldBe true)
          }

      }

    }

    Scenario("context with same name exists --> fail") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(existingContext) =>

          val context = existingContext.copy(id = UUIDUtil.uuidStr)

          // test
          ContextManager.create(context) flatMap { created =>

            // verify
            created shouldBe None
            ContextManager.findById(existingContext.id) map(_ should be(Some(existingContext)))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            ContextManager.delete(existingContext.id).map(_ shouldBe true)
          }

      }

    }

  }

  Feature("update()") {

    Scenario("context.id does not exist --> fail") {

      // test
      ContextManager.update(DefaultModels.context()) flatMap { updated =>

        // verify
        updated shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    Scenario("context.id exists --> success") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          val update = context.copy(displayName = s"${context.displayName}-test")

          // test
          ContextManager.update(update) flatMap { result =>

            // verify
            result shouldBe Some(update)
            ContextManager.findById(update.id) map(_ should be(Some(update)))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            ContextManager.delete(context.id).map(_ shouldBe true)
          }

      }

    }

  }

  Feature("findById()") {

    Scenario("context.id does not exist --> fail") {

      // test
      ContextManager.findById(UUIDUtil.uuidStr) flatMap { created =>

        // verify
        created shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    Scenario("context.id exists --> success") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          // test
          ContextManager.findById(context.id) flatMap { result =>

            // verify
            result shouldBe Some(context)
            ContextManager.findById(result.get.id) map(_ should be(result))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            ContextManager.delete(context.id).map(_ shouldBe true)
          }

      }

    }

  }

  Feature("findByName()") {

    Scenario("context.name does not exist --> fail") {

      // test
      ContextManager.findByName("automated-test") flatMap { created =>

        // verify
        created shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    Scenario("context.name exists --> success") {

      // prepare
      ContextManager.create(DefaultModels.context()) flatMap {

        case None => fail("failed during preparation")

        case Some(context) =>

          // test
          ContextManager.findByName(context.displayName) flatMap { result =>

            // verify
            result shouldBe Some(context)
            ContextManager.findById(result.get.id) map(_ should be(result))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            ContextManager.delete(context.id).map(_ shouldBe true)
          }

      }

    }

  }

  Feature("delete()") {

    Scenario("context.id does not exist --> fail") {

      // test
      ContextManager.delete(UUIDUtil.uuidStr) flatMap { result =>

        // verify
        result shouldBe false
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    Scenario("context.id exists --> success") {

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
