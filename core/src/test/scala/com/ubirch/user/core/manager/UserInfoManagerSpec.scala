package com.ubirch.user.core.manager

import com.ubirch.user.core.manager.testUtils.DataHelpers
import com.ubirch.user.core.manager.util.UserInfoUtil
import com.ubirch.user.model.rest.{SimpleUserContext, UserInfo, UserUpdate}
import com.ubirch.user.testTools.db.mongo.MongoSpec

/**
  * author: cvandrei
  * since: 2017-04-25
  */
class UserInfoManagerSpec extends MongoSpec {

  private val dataHelpers = new DataHelpers

  feature("getInfo()") {

    scenario("user does not exist") {

      // prepare
      val userContext = defaultSimpleUserContext()

      // test
      UserInfoManager.getInfo(userContext) map { result =>

        // verify
        result shouldBe None

      }

    }

    scenario("user exists - without groups") {

      // prepare
      for {

        contextOpt <- dataHelpers.createContext()
        userOpt <- dataHelpers.createUser()

        user = userOpt.get
        userContext = SimpleUserContext(
          context = contextOpt.get.displayName,
          providerId = user.providerId,
          userId = user.externalId
        )

        // test
        result <- UserInfoManager.getInfo(userContext)

      } yield {

        // verify
        val expected = UserInfo(
          displayName = user.displayName,
          locale = user.locale
        )
        result shouldBe Some(expected)

      }

    }

    scenario("non-admin user exists - with myGroups") {

      // prepare
      for {

        contextOpt <- dataHelpers.createContext()
        userOpt <- dataHelpers.createUser()
        myGroup1Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = userOpt, adminGroup = None)
        myGroup2Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = userOpt, adminGroup = None)

        user = userOpt.get
        userContext = SimpleUserContext(
          context = contextOpt.get.displayName,
          providerId = user.providerId,
          userId = user.externalId
        )

        // test
        result <- UserInfoManager.getInfo(userContext)

      } yield {

        // verify
        val myGroups = UserInfoUtil.toUserInfoGroups(Set(myGroup1Opt.get, myGroup2Opt.get))
        val expected = UserInfo(
          displayName = user.displayName,
          locale = user.locale,
          myGroups = myGroups
        )
        result shouldBe Some(expected)

      }

    }

    scenario("admin user exists - with myGroups") {

      // prepare
      for {

        contextOpt <- dataHelpers.createContext()
        userOpt <- dataHelpers.createUser()
        myGroup1Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = userOpt, adminGroup = Some(true))
        myGroup2Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = userOpt, adminGroup = None)

        user = userOpt.get
        userContext = SimpleUserContext(
          context = contextOpt.get.displayName,
          providerId = user.providerId,
          userId = user.externalId
        )

        // test
        result <- UserInfoManager.getInfo(userContext)

      } yield {

        // verify
        val myGroups = UserInfoUtil.toUserInfoGroups(Set(myGroup1Opt.get, myGroup2Opt.get))
        val expected = UserInfo(
          displayName = user.displayName,
          locale = user.locale,
          myGroups = myGroups
        )
        result shouldBe Some(expected)

      }

    }

    scenario("user exists - with allowedGroups") {

      // prepare
      for {

        contextOpt <- dataHelpers.createContext()
        user1Opt <- dataHelpers.createUser()
        user2Opt <- dataHelpers.createUser()
        allowedGroup1Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = user2Opt, adminGroup = None, user1Opt)
        allowedGroup2Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = user2Opt, adminGroup = None, user1Opt)

        user = user1Opt.get
        userContext = SimpleUserContext(
          context = contextOpt.get.displayName,
          providerId = user.providerId,
          userId = user.externalId
        )

        // test
        result <- UserInfoManager.getInfo(userContext)

      } yield {

        // verify
        val allowedGroups = UserInfoUtil.toUserInfoGroups(Set(allowedGroup1Opt.get, allowedGroup2Opt.get))
        val expected = UserInfo(
          displayName = user.displayName,
          locale = user.locale,
          allowedGroups = allowedGroups
        )
        result shouldBe Some(expected)

      }

    }

    scenario("user exists - with myGroups && allowedGroups") {

      // prepare
      for {

        contextOpt <- dataHelpers.createContext()
        user1Opt <- dataHelpers.createUser()
        myGroup1Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = user1Opt, adminGroup = None)
        myGroup2Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = user1Opt, adminGroup = None)
        user2Opt <- dataHelpers.createUser()
        allowedGroup1Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = user2Opt, adminGroup = None, user1Opt)
        allowedGroup2Opt <- dataHelpers.createGroup(contextOpt = contextOpt, ownerOpt = user2Opt, adminGroup = None, user1Opt)

        user = user1Opt.get
        userContext = SimpleUserContext(
          context = contextOpt.get.displayName,
          providerId = user.providerId,
          userId = user.externalId
        )

        // test
        result <- UserInfoManager.getInfo(userContext)

      } yield {

        // verify
        val myGroups = UserInfoUtil.toUserInfoGroups(Set(myGroup1Opt.get, myGroup2Opt.get))
        val allowedGroups = UserInfoUtil.toUserInfoGroups(Set(allowedGroup1Opt.get, allowedGroup2Opt.get))
        val expected = UserInfo(
          displayName = user.displayName,
          locale = user.locale,
          myGroups = myGroups,
          allowedGroups = allowedGroups
        )
        result shouldBe Some(expected)

      }

    }

  }

  feature("update()") {

    scenario("user does not exist") {

      // prepare
      val userContext = defaultSimpleUserContext()
      val userUpdate = UserUpdate("new display name")

      // test
      UserInfoManager.update(userContext, userUpdate) map { result =>

        // verify
        result shouldBe None

      }

    }

    scenario("user does not change") {

      // prepare
      for {

        contextOpt <- dataHelpers.createContext()
        user1Opt <- dataHelpers.createUser()

        user = user1Opt.get
        userContext = SimpleUserContext(
          context = contextOpt.get.displayName,
          providerId = user.providerId,
          userId = user.externalId
        )
        userUpdate = UserUpdate(user.displayName)

        // test
        result <- UserInfoManager.update(userContext, userUpdate)

      } yield {

        // verify
        val expected = UserInfo(displayName = user.displayName, locale = user.locale)
        result shouldBe Some(expected)

      }

    }

    scenario("user.displayName changes") {

      // prepare
      for {

        contextOpt <- dataHelpers.createContext()
        user1Opt <- dataHelpers.createUser()

        user = user1Opt.get
        userContext = SimpleUserContext(
          context = contextOpt.get.displayName,
          providerId = user.providerId,
          userId = user.externalId
        )
        userUpdate = UserUpdate(s"${user.displayName}-updated")

        // test
        result <- UserInfoManager.update(userContext, userUpdate)

      } yield {

        // verify
        val expected = UserInfo(displayName = userUpdate.displayName, locale = user.locale)
        result shouldBe Some(expected)

      }

    }

  }

  private def defaultSimpleUserContext(context: String = "context-test",
                                       providerId: String = "provider-test",
                                       userId: String = "user-id-test",
                                       userName: String = "some-user-name-test",
                                       locale: String = "en"
                                      ): SimpleUserContext = {

    SimpleUserContext(
      context = context,
      providerId = providerId,
      userId = userId
    )

  }

}
