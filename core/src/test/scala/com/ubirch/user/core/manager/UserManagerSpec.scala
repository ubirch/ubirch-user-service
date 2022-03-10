package com.ubirch.user.core.manager

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Deactivate
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
        UserManager.delete(user.id).map(_ shouldBe true)
      }

    }

    Scenario("user with action --> success") {

      // prepare
      val user = DefaultModels.user().copy(action = Some(Deactivate))

      // test
      UserManager.create(user) flatMap { created =>

        // verify
        created shouldBe Some(user)
        Thread.sleep(200)
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)
        UserManager.delete(user.id).map(_ shouldBe true)
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
        UserManager.delete(user.id).map(_ shouldBe true)
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
              result.isDefined shouldBe true
              result shouldBe Some(update.copy(updated = result.get.updated))
              UserManager.findById(update.id) map (_ should be(Some(update)))
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
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
              UserManager.delete(user.id).map(_ shouldBe true)
          }

      }

    }

    Scenario("update email to None--> success") {

      val email1 = "b@d.de"
      val email2 = None

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
              UserManager.delete(user.id).map(_ shouldBe true)
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
              result.isDefined shouldBe true
              result shouldBe Some(update.copy(updated = result.get.updated))
              UserManager.findById(update.id) map (_ should be(Some(update)))
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
          }

      }

    }

  }


  Feature("findById()") {

    Scenario("user.id does not exist --> fail") {

      mongoTestUtils.countAll(collection).flatMap { before =>
        // test
        UserManager.findById(UUIDUtil.uuidStr) flatMap {
          created =>

            // verify
            created shouldBe None
            mongoTestUtils.countAll(collection) map (_ - before shouldBe 0)

        }
      }
    }

    Scenario("user.id exists --> success") {

      val user = DefaultModels.user().copy(action = Some(Deactivate))

      // prepare
      UserManager.create(user) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findById(user.id) flatMap {
            result =>

              // verify
              result shouldBe Some(user)
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
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
          ) flatMap {
            result =>

              // verify
              result shouldBe Some(user)
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
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
          ) flatMap {
            result =>

              // verify
              result shouldBe Some(user)
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
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
          ) flatMap {
            result =>

              // verify
              result shouldBe Some(user)
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
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
            providerId = s"${
              user.providerId
            }-test",
            externalUserId = user.externalId
          ) flatMap {
            result =>

              // verify
              result shouldBe None
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
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
              UserManager.delete(user.id).map(_ shouldBe true)
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
              UserManager.delete(user.id).map(_ shouldBe true)
          }
      }
    }
  }

  Feature("findByEmail()") {

    Scenario("email=None --> None") {

      // prepare
      UserManager.create(DefaultModels.user()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByExternalId("test@ubirch.com") flatMap {
            result =>

              // verify
              result.isEmpty shouldBe true
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
          }
      }
    }

    Scenario("email=Some; search with another email address --> None") {

      // prepare
      val email1 = "test1@ubirch.com"
      val email2 = "test2@ubirch.com"
      UserManager.create(DefaultModels.user(email = Some(email1))) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          UserManager.findByExternalId(email2) flatMap {
            result =>

              // verify
              result.isEmpty shouldBe true
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
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
          UserManager.findByExternalId(user.externalId) flatMap {
            result =>

              // verify
              result shouldBe Some(user)
              mongoTestUtils.countAll(collection) map (_ shouldBe 1)
              UserManager.delete(user.id).map(_ shouldBe true)
          }
      }
    }
  }

  Feature("delete()") {

    Scenario("user.id does not exist --> fail") {

      mongoTestUtils.countAll(collection).flatMap { before =>
        // test
        UserManager.delete(UUIDUtil.uuidStr) flatMap {
          result =>
            // verify
            result shouldBe false
            mongoTestUtils.countAll(collection) map (_-before shouldBe 0)
        }
      }
    }

    Scenario("user.id exists --> success") {

      mongoTestUtils.countAll(collection).flatMap { before =>
        // prepare
        UserManager.create(DefaultModels.user()) flatMap {

          case None => fail("failed during preparation")

          case Some(user) =>

            // test
            UserManager.delete(user.id) flatMap {
              result =>

                // verify
                result shouldBe true
                mongoTestUtils.countAll(collection) map (_ - before shouldBe 0)
            }
        }
      }
    }
  }

  Feature("updateMany()") {

    Scenario("user.id does not exist --> fail") {

      // prepare
      val user1 = DefaultModels.user()

      // test
      for {
        result <-  UserManager.updateMany(Seq(user1))
      } yield {
        //verify
        result.isLeft shouldBe true
        val msg = result.swap.getOrElse(fail("left should be defined"))
        msg.contains(s"error on updating users in mongoDB with ids") shouldBe true
      }
    }

    Scenario("Only one of two user does not exist --> fail") {

      // prepare
      val user1 = DefaultModels.user()
      val user2 = DefaultModels.user()

      for {
        user1 <- UserManager.create(DefaultModels.user())
        _ = user1.isDefined shouldBe true
        user1Update = user1.get.copy(action = Some(Deactivate))
        user2Update = DefaultModels.user()
        // test
        result <- UserManager.updateMany(Seq(user1Update, user2Update))
      } yield {
        // verify
        user1.isDefined shouldBe true
        result.isLeft shouldBe true
        val msg = result.swap.getOrElse(fail("left should be defined"))
        msg.contains(s"error on updating users in mongoDB with ids") shouldBe true
      }
    }

    Scenario("update action --> success") {

      for {
        // prepare
        user1 <- UserManager.create(DefaultModels.user())
        user2 <- UserManager.create(DefaultModels.user())
        _ = user1.isDefined shouldBe true
        _ = user2.isDefined shouldBe true
        user1Update = user1.get.copy(action = Some(Deactivate))
        user2Update = user2.get.copy(action = Some(Deactivate))
        // test
        updatedUsers <- UserManager.updateMany(Seq(user1Update, user2Update))
        // verify
        getUser1 <- UserManager.findById(user1Update.id)
        getUser2 <- UserManager.findById(user2Update.id)
      } yield {
        getUser1.isDefined shouldBe true
        getUser2.isDefined shouldBe true
        updatedUsers.isRight shouldBe true
        val updated = updatedUsers.getOrElse(fail("should be right with users as context"))
        updated.size == 2 shouldBe true
        updated.contains(getUser1.get) shouldBe true
        updated.contains(getUser2.get) shouldBe true
      }
    }
  }

}
