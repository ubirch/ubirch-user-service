package com.ubirch.user.core.manager

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.tools.DefaultModels
import com.ubirch.user.testTools.db.mongo.MongoSpec
import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-04-06
  */
class UserManagerSpec extends MongoSpec {

  private val collection = Config.mongoCollectionUser

  feature("create()") {

    scenario("user does NOT exist --> success") {

      // prepare
      val user = DefaultModels.user()

      // test
      UserManager.create(user) flatMap { created =>

        // verify
        created shouldBe Some(user)
        Thread.sleep(200)
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)

      }

    }

    scenario("user already exists --> fail") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(existingUser) =>

          // test
          UserManager.create(existingUser) flatMap { created =>

            // verify
            created shouldBe None
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("update()") {

    scenario("user.id does not exist --> fail") {

      // prepare
      val user = DefaultModels.user()

      // test
      UserManager.update(user) flatMap { updated =>

        // verify
        updated shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    scenario("user.id exists --> success") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          val update = user.copy(displayName = s"${user.displayName}-test")

          // test
          UserManager.update(update) flatMap { result =>

            // verify
            result shouldBe Some(update)
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("findById()") {

    scenario("user.id does not exist --> fail") {

      // test
      UserManager.findById(UUIDUtil.uuid) flatMap { created =>

        // verify
        created shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    scenario("user.id exists --> success") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findById(user.id) flatMap { result =>

            // verify
            result shouldBe Some(user)
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("findByProviderIdAndExternalId()") {

    scenario("user.providerId: exists; user.externalId: exists --> success") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByProviderIdAndExternalId(
            providerId = user.providerId,
            externalUserId = user.externalId
          ) flatMap { result =>

            // verify
            result shouldBe Some(user)
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    scenario("user.providerId: does not exist; user.externalId: exists --> fail") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByProviderIdAndExternalId(
            providerId = s"${user.providerId}-test",
            externalUserId = user.externalId
          ) flatMap { result =>

            // verify
            result shouldBe None
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    scenario("user.providerId: exists; user.externalId: does not exist --> fail") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByProviderIdAndExternalId(
            providerId = user.providerId,
            externalUserId = s"${user.externalId}-test"
          ) flatMap { result =>

            // verify
            result shouldBe None
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    scenario("user.providerId: exists; user.externalId: exists --> fail") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByProviderIdAndExternalId(
            providerId = s"${user.providerId}-test",
            externalUserId = s"${user.externalId}-test"
          ) flatMap { result =>

            // verify
            result shouldBe None
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  feature("delete()") {

    scenario("user.id does not exist --> fail") {

      // test
      UserManager.delete(UUIDUtil.uuid) flatMap { result =>

        // verify
        result shouldBe false
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    scenario("user.id exists --> success") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.delete(user.id) flatMap { result =>

            // verify
            result shouldBe true
            mongoTestUtils.countAll(collection) map (_ shouldBe 0)

          }

      }

    }

  }

}
