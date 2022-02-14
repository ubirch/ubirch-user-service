package com.ubirch.user.core.manager

import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.testUtils.DataHelpers
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.user.model.rest.{UserContext, UserInfo}
import com.ubirch.user.testTools.db.mongo.MongoSpec
import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-04-24
  */
class RegistrationManagerSpec extends MongoSpec {

  private val dataHelpers = new DataHelpers

  Feature("register()") {

    Scenario(
      """exists?
        |  context: no
        |  user: no
        |  group: no
        |
        | --> nothing happens
      """.stripMargin
    ) {

      // prepare
      val userContext = defaultUserContext()

      // test
      RegistrationManager.register(userContext) flatMap { result =>

        // verify
        result shouldBe None

        mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 0)
        mongoTestUtils.countAll(Config.mongoCollectionUser) map (_ shouldBe 0)
        mongoTestUtils.countAll(Config.mongoCollectionGroup) map (_ shouldBe 0)

      }
    }

    Scenario(
      """exists?
        |  context: no
        |  user: yes
        |  group: no
        |
        | --> nothing happens
      """.stripMargin
    ) {

      // prepare
      val userContext = defaultUserContext()

      dataHelpers.createUser(
        displayName = userContext.userName,
        providerId = userContext.providerId,
        externalId = userContext.externalUserId
      ) flatMap {

        case None => fail("failed to create user during preparation")

        case Some(_: User) =>

          // test
          RegistrationManager.register(userContext) flatMap { result =>

            // verify
            result shouldBe None

            mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 0)
            mongoTestUtils.countAll(Config.mongoCollectionUser) map (_ shouldBe 1)
            mongoTestUtils.countAll(Config.mongoCollectionGroup) map (_ shouldBe 0)

          }

      }

    }

    Scenario(
      """exists?
        |  context: no
        |  user: yes
        |  group: yes
        |
        | --> nothing happens
      """.stripMargin
    ) {

      // prepare
      val userContext = defaultUserContext()

      dataHelpers.createUser(
        displayName = userContext.userName,
        providerId = userContext.providerId,
        externalId = userContext.externalUserId
      ) flatMap { userOpt =>

        dataHelpers.createGroup(
          contextId = UUIDUtil.uuidStr,
          ownerOpt = userOpt,
          adminGroup = None
        ) flatMap {

          case None => fail("failed to create group during preparation")

          case Some(_: Group) =>

            // test
            RegistrationManager.register(userContext) flatMap { result =>

              // verify
              result shouldBe None

              mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 0)
              mongoTestUtils.countAll(Config.mongoCollectionUser) map (_ shouldBe 1)
              mongoTestUtils.countAll(Config.mongoCollectionGroup) map (_ shouldBe 1)

            }

        }

      }

    }

    Scenario(
      """exists?
        |  context: yes
        |  user: no
        |  group: no
        |
        | -- create user without email address
        |
        | --> create user and group
      """.stripMargin
    ) {

      // prepare
      val userContext: UserContext = defaultUserContext()

      dataHelpers.createContext(displayName = userContext.context) flatMap {

        case None => fail("failed to create context during preparation")

        case Some(_: Context) =>

          // test
          RegistrationManager.register(userContext) flatMap {

            // verify
            case None => fail("expected a Some result")

            case Some(result: UserInfo) =>

              mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 1)
              mongoTestUtils.countAll(Config.mongoCollectionUser) map (_ shouldBe 1)
              mongoTestUtils.countAll(Config.mongoCollectionGroup) map (_ shouldBe 1)

              val userName = userContext.userName
              result.displayName shouldBe userName
              result.myGroups.size shouldBe 1
              result.myGroups.head.displayName shouldBe userName
              result.myGroups.head.adminGroup shouldBe None
              result.allowedGroups.isEmpty shouldBe true

          }

      }

    }

    Scenario(
      """exists?
        |  context: yes
        |  user: yes (different name and locale than existing user...as it's being ignored)
        |  group: no
        |
        | --> create missing group
      """.stripMargin
    ) {

      // prepare
      val userContext = defaultUserContext()

      dataHelpers.createContextIfNotExists(displayName = userContext.context) flatMap {

        case None => fail("failed to create context during preparation")

        case Some(_: Context) =>
          val otherLocale = if (userContext.locale == "en") {
            "de"
          } else {
            "en"
          }
          dataHelpers.createUser(
            displayName = s"${userContext.userName}-actual",
            providerId = userContext.providerId,
            externalId = userContext.externalUserId,
            locale = otherLocale
          ) flatMap {

            case None => fail("failed to create user during preparation")

            case Some(user: User) =>

              // test
              RegistrationManager.register(userContext) flatMap {

                // verify
                case None => fail("expected a Some result")

                case Some(result: UserInfo) =>

                  mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ shouldBe 1)
                  mongoTestUtils.countAll(Config.mongoCollectionUser) map (_ shouldBe 1)
                  mongoTestUtils.countAll(Config.mongoCollectionGroup) map (_ shouldBe 1)

                  result.displayName shouldBe user.displayName
                  result.myGroups.size shouldBe 1
                  result.myGroups.head.displayName shouldBe userContext.userName
                  result.myGroups.head.adminGroup shouldBe None
                  result.allowedGroups.isEmpty shouldBe true

              }

          }

      }

    }

    Scenario(
      """exists?
        |  context: yes
        |  user: yes
        |  group: yes
        |
        | --> nothing happens
      """.stripMargin
    ) {

      // prepare
      val userContext = defaultUserContext()

      dataHelpers.createContextIfNotExists(displayName = userContext.context) flatMap { contextOpt =>

        val beforeContext = mongoTestUtils.countAll(Config.mongoCollectionContext)
        val beforeUser = mongoTestUtils.countAll(Config.mongoCollectionUser)
        val beforeGroup = mongoTestUtils.countAll(Config.mongoCollectionGroup)

        dataHelpers.createUser(
          displayName = userContext.userName,
          providerId = userContext.providerId,
          externalId = userContext.externalUserId
        ) flatMap { userOpt =>

          dataHelpers.createGroup(
            contextOpt = contextOpt,
            ownerOpt = userOpt,
            adminGroup = None
          ) flatMap {

            case None => fail("failed to create group during preparation")

            case Some(_: Group) =>

              // test
              RegistrationManager.register(userContext) flatMap { result =>

                // verify
                result shouldBe None

                beforeContext.flatMap( before => mongoTestUtils.countAll(Config.mongoCollectionContext) map (_ - before shouldBe 1))
                beforeUser.flatMap( before => mongoTestUtils.countAll(Config.mongoCollectionUser) map (_ - before shouldBe 1))
                beforeGroup.flatMap( before => mongoTestUtils.countAll(Config.mongoCollectionGroup) map (_ -before shouldBe 1))

              }

          }

        }

      }

    }

  }

  private val r = scala.util.Random

  private def defaultUserContext(): UserContext = {

    UserContext(
      context = "trackle-dev",
      providerId = "google",
      externalUserId = r.nextInt(10000000).toString,
      userName = "user display name",
      locale = "en"
    )

  }

}
