package com.ubirch.user.core.manager

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.tools.DefaultModels
import com.ubirch.user.testTools.db.mongo.MongoSpec
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.uuid.UUIDUtil
import org.scalatest.concurrent.ScalaFutures


/**
  * author: cvandrei
  * since: 2017-04-06
  */
class UserManagerSpec extends MongoSpec with ScalaFutures {

  private val collection = Config.mongoCollectionUser

  Feature("create()") {

    Scenario("user does NOT exist --> success") {

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

    Scenario("user with email does NOT exist --> success") {

      val emailAdr = " A@a.de "
      val cleanEmailAdr = emailAdr.toLowerCase.trim
      // prepare
      val user = DefaultModels.user(email = Some(emailAdr))

      // test
      UserManager.create(user) flatMap { created =>

        // verify
        created.isDefined shouldBe true
        created.get.providerId shouldBe user.providerId
        created.get.externalId shouldBe user.externalId
        created.get.locale shouldBe user.locale
        created.get.displayName shouldBe user.displayName
        created.get.email.isDefined shouldBe true
        created.get.email.get shouldBe cleanEmailAdr
        created.get.hashedEmail.get shouldBe HashUtil.sha512HexString(cleanEmailAdr)

        Thread.sleep(200)
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)

      }

    }

    Scenario("user already exists --> fail") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(existingUser) =>

          // test
          Thread.sleep(300)

          val f = UserManager.create(existingUser)

          ScalaFutures.whenReady(f.failed) { e =>
            e shouldBe a[Exception]
          }
      }

    }

  }

  Feature("update()") {

    Scenario("user.id does not exist --> fail") {

      // prepare
      val user = DefaultModels.user()

      // test
      val f = UserManager.update(user)
      ScalaFutures.whenReady(f.failed) { e =>
        e shouldBe a[Exception]
      }

    }

    Scenario("update displayName --> success") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          val update = user.copy(
            displayName = s"${
              user.displayName
            }-test"
          )

          // test
          UserManager.update(update) flatMap {
            result =>

              // verify
              result shouldBe Some(update)
              UserManager.findById(update.id) map (_ should be(Some(update)))
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    Scenario("update email --> success") {

      val email1 = "b@d.de"
      val email2 = "c@e.de"
      val hashedEmail2 = HashUtil.sha512HexString(email2)

      // prepare
      UserManager.create(DefaultModels.user(email = Some(email1))) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          val update = user.copy(
            email = Some(email2)
          )

          // test
          UserManager.update(update) flatMap {
            result =>

              // verify
              result.isDefined shouldBe true
              result.get.email.isDefined shouldBe true
              result.get.email.get shouldBe email2
              result.get.hashedEmail.isDefined shouldBe true
              result.get.hashedEmail.get shouldBe hashedEmail2

              UserManager.findById(update.id) map (_ should be(Some(update)))
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    Scenario("update email to None--> success") {

      val email1 = "b@d.de"
      val email2 = None
      val hashedEmail2 = None

      // prepare
      UserManager.create(DefaultModels.user(email = Some(email1))) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          val update = user.copy(
            email = email2
          )

          // test
          UserManager.update(update) flatMap {
            result =>

              // verify
              result.isDefined shouldBe true
              result.get.email.isEmpty shouldBe true
              result.get.hashedEmail.isEmpty shouldBe true

              UserManager.findById(update.id) map (_ should be(Some(update)))
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    Scenario("update locale --> success") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          val newLocale = if (user.locale == "en") {
            "de"
          } else {
            "en"
          }
          val update = user.copy(locale = newLocale)

          // test
          UserManager.update(update) flatMap {
            result =>

              // verify
              result shouldBe Some(update)
              UserManager.findById(update.id) map (_ should be(Some(update)))
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  Feature("findById()") {

    Scenario("user.id does not exist --> fail") {

      // test
      UserManager.findById(UUIDUtil.uuidStr) flatMap {
        created =>

          // verify
          created shouldBe None
          mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    Scenario("user.id exists --> success") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findById(user.id) flatMap {
            result =>

              // verify
              result shouldBe Some(user)
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  Feature("findByProviderIdAndExternalId()") {

    Scenario("user.providerId: exists; user.externalId: exists --> success") {

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

    Scenario("user.providerId: exists (BASE64 encoded; all lower-case); user.externalId: exists --> success") {

      // prepare
      val userPrep = DefaultModels.user(
        providerId = "ubirchToken",
        externalId = "hcdapzt1mcab/62fexw+6+b0ierknvvmfqnnb0tc4wy+lwdx+ejphwjhvin3fgq5b8paltamehokvrw0usufxq=="
      )
      UserManager.create(userPrep) flatMap {

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

    Scenario("user.providerId: exists (BASE64 encoded with some upper-cases; url encoded); user.externalId: exists --> success") {

      // prepare
      val userPrep = DefaultModels.user(
        providerId = "ubirchToken",
        externalId = "HcdAPzT1McaB/62feXw+6+B0iErKNvVMFQnnb0Tc4wy+lWdx+EJphwjHVin3fgq5b8paLtAmEHOkVrW0uSufXQ=="
      )
      UserManager.create(userPrep) flatMap {

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

    Scenario("user.providerId: does not exist; user.externalId: exists --> fail") {

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

    Scenario("user.providerId: exists; user.externalId: does not exist --> fail") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByProviderIdAndExternalId(
            providerId = user.providerId,
            externalUserId = s"${
              user.externalId
            }-test"
          ) flatMap {
            result =>

              // verify
              result shouldBe None
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    Scenario("user.providerId: exists; user.externalId: exists --> fail") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByProviderIdAndExternalId(
            providerId = s"${
              user.providerId
            }-test",
            externalUserId = s"${
              user.externalId
            }-test"
          ) flatMap {
            result =>

              // verify
              result shouldBe None
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  Feature("findByEmail()") {

    Scenario("email=None --> None") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(_) =>

          // test
          UserManager.findByExternalId("test@ubirch.com") flatMap {
            result =>

              // verify
              result.isEmpty shouldBe true
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    Scenario("email=Some; search with another email address --> None") {

      // prepare
      val email1 = "test1@ubirch.com"
      val email2 = "test2@ubirch.com"
      UserManager.create(DefaultModels.user(email = Some(email1))) flatMap {

        case None => fail("failed during preparation")

        case Some(_) =>

          // test
          UserManager.findByExternalId(email2) flatMap {
            result =>

              // verify
              result.isEmpty shouldBe true
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

    Scenario("email=Some; search with same email address --> Some") {

      // prepare
      val email = "test@ubirch.com"
      UserManager.create(DefaultModels.user(email = Some(email))) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByExternalId(email) flatMap {
            result =>

              // verify
              result shouldBe Some(user)
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)

          }

      }

    }

  }

  Feature("delete()") {

    Scenario("user.id does not exist --> fail") {

      // test
      UserManager.delete(UUIDUtil.uuidStr) flatMap {
        result =>

          // verify
          result shouldBe false
          mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    Scenario("user.id exists --> success") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.delete(user.id) flatMap {
            result =>

              // verify
              result shouldBe true
              mongoTestUtils.countAll(collection) map (_ shouldBe 0)

          }

      }

    }

  }

}
