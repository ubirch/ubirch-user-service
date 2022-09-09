package com.ubirch.user.core.manager

import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.util.DBException
import com.ubirch.user.model.db.tools.DefaultModels
import com.ubirch.user.testTools.db.mongo.MongoSpec
import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-04-06
  */
class GroupManagerSpec extends MongoSpec {

  private val collection = Config.mongoCollectionGroup

  Feature("create()") {

    Scenario("non-admin group does NOT exist --> success") {

      // prepare
      val group = DefaultModels.group()

      // test
      GroupManager.create(group) flatMap { created =>

        // verify
        created shouldBe Some(group)
        ContextManager.findById(created.get.id) map(_ should be(created))
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)
        GroupManager.delete(group.id).map(_ shouldBe true)

      }

    }

    Scenario("admin group does NOT exist --> success") {

      // prepare
      val group = DefaultModels.group(adminGroup = Some(true))

      // test
      GroupManager.create(group) flatMap { created =>

        // verify
        created shouldBe Some(group)
        ContextManager.findById(created.get.id) map(_ should be(created))
        mongoTestUtils.countAll(collection) map (_ shouldBe 1)
        GroupManager.delete(group.id).map(_ shouldBe true)

      }

    }

    Scenario("non-admin group already exists --> fail") {

      // prepare
      GroupManager.create(DefaultModels.group()) flatMap {

        case None => fail("failed during preparation")

        case Some(existingGroup) =>
          // test
          GroupManager.create(existingGroup).recover {
            case ex =>
              assert(ex.isInstanceOf[DBException])
              None
          }.flatMap { created =>
            // verify
            created shouldBe None
            ContextManager.findById(existingGroup.id) map (_ should be(Some(created)))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            GroupManager.delete(existingGroup.id).map(_ shouldBe true)

          }

      }

    }

    Scenario("admin group already exists --> fail") {

      // prepare
      GroupManager.create(DefaultModels.group(adminGroup = Some(true))) flatMap {

        case None => fail("failed during preparation")

        case Some(existingGroup) =>
          // test
          GroupManager.create(existingGroup).recover {
            case ex =>
              assert(ex.isInstanceOf[DBException])
              None
          }.flatMap { created =>
            // verify
            created shouldBe None
            ContextManager.findById(existingGroup.id) map (_ should be(Some(existingGroup)))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            GroupManager.delete(existingGroup.id).map(_ shouldBe true)
          }

      }

    }

  }

  Feature("update()") {

    Scenario("group.id does not exist --> fail") {

      // prepare
      val group = DefaultModels.group()

      // test
      GroupManager.update(group).recover {
        case ex =>
          assert(ex.isInstanceOf[DBException])
          None
      }.flatMap { updated =>
        // verify
        updated shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)
      }

    }

    Scenario("group.id exists --> success") {

      // prepare
      GroupManager.create(DefaultModels.group()) flatMap {

        case None => fail("failed during preparation")

        case Some(group) =>

          val update = group.copy(displayName = s"${group.displayName}-test")

          // test
          GroupManager.update(update) flatMap { result =>

            // verify
            result shouldBe Some(update)
            ContextManager.findById(result.get.id) map(_ should be(Some(update)))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            GroupManager.delete(group.id).map(_ shouldBe true)
          }

      }

    }

    Scenario("group.id exists and promote to admin --> success") {

      // prepare
      GroupManager.create(DefaultModels.group()) flatMap {

        case None => fail("failed during preparation")

        case Some(group) =>

          val update = group.copy(adminGroup = Some(true))

          // test
          GroupManager.update(update) flatMap { result =>

            // verify
            result shouldBe Some(update)
            ContextManager.findById(group.id) map(_ should be(result))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            GroupManager.delete(group.id).map(_ shouldBe true)
          }

      }

    }

  }

  Feature("findById()") {

    Scenario("group.id does not exist --> fail") {

      // test
      GroupManager.findById(UUIDUtil.uuidStr) flatMap { created =>

        // verify
        created shouldBe None
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)
      }

    }

    Scenario("non-admin group.id exists --> success") {

      // prepare
      GroupManager.create(DefaultModels.group()) flatMap {

        case None => fail("failed during preparation")

        case Some(group) =>

          // test
          GroupManager.findById(group.id) flatMap { result =>

            // verify
            result shouldBe Some(group)
            ContextManager.findById(group.id) map(_ should be(Some(group)))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            GroupManager.delete(group.id).map(_ shouldBe true)
          }

      }

    }

    Scenario("admin group.id exists --> success") {

      // prepare
      GroupManager.create(DefaultModels.group(adminGroup = Some(true))) flatMap {

        case None => fail("failed during preparation")

        case Some(group) =>

          // test
          GroupManager.findById(group.id) flatMap { result =>

            // verify
            result shouldBe Some(group)
            ContextManager.findById(group.id) map(_ should be(Some(group)))
            mongoTestUtils.countAll(collection) map (_ shouldBe 1)
            GroupManager.delete(group.id).map(_ shouldBe true)
          }

      }

    }

  }

  Feature("delete()") {

    Scenario("group.id does not exist --> fail") {

      // test
      GroupManager.delete(UUIDUtil.uuidStr) flatMap { result =>

        // verify
        result shouldBe false
        mongoTestUtils.countAll(collection) map (_ shouldBe 0)

      }

    }

    Scenario("group.id exists --> success") {

      // prepare
      GroupManager.create(DefaultModels.group()) flatMap {

        case None => fail("failed during preparation")

        case Some(user) =>

          // test
          GroupManager.delete(user.id) flatMap { result =>

            // verify
            result shouldBe true
            mongoTestUtils.countAll(collection) map (_ shouldBe 0)

          }

      }

    }

  }

}
