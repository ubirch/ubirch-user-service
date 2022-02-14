package com.ubirch.user.core.actor

import com.ubirch.user.core.manager.UserManager
import com.ubirch.user.model.db.tools.DefaultModels
import com.ubirch.user.model.db.{Activate, Deactivate}
import com.ubirch.user.model.rest.ActivationUpdate._
import com.ubirch.user.model.rest.{ActivationUpdate, UserActivationUpdate}
import com.ubirch.user.testTools.db.mongo.MongoSpec
import com.ubirch.util.date.DateUtil
import io.jvm.uuid.UUID

class UserActorLogicSpec extends MongoSpec {

  private val userLogic = new UserActorLogic {}

  class UserActorLogicImp extends UserActorLogic

  Feature("validate()") {

    Scenario("executionDate in the past -> fail") {

      // prepare
      val yesterday = DateUtil.nowUTC.minusDays(1)
      val user1 = DefaultModels.user()
      val user2 = DefaultModels.user()
      val activations =
        ActivationUpdate(Seq(
          UserActivationUpdate(user1.externalId, activate = true, executionDate = Some(yesterday)),
          UserActivationUpdate(user2.externalId, activate = true, None)))

      for {
        //test
        result <-  userLogic.validate(activations, Seq(user1, user2))
      } yield {
        //verify
        result shouldBe Left(errorHeader + lineBreak + activations.updates.head.toErrorCsv + dateInPast)
      }
    }

    Scenario("state same as existing -> fail") {



      // prepare
      val user1 = DefaultModels.user()
      val user2 = DefaultModels.user()
      val user3 = DefaultModels.user()
      val activations =
        ActivationUpdate(Seq(
          UserActivationUpdate(user1.externalId, activate = false),
          UserActivationUpdate(user2.externalId, activate = true),
          UserActivationUpdate(user3.externalId, activate = false)))

      for {
        //test
        result <-  userLogic.validate(activations, Seq(user1, user2, user3))
      } yield {
        //verify
        val errorRow1 = activations.updates.head.toErrorCsv + targetStateWrong(false.toString)
        val errorRow2 = activations.updates.last.toErrorCsv + targetStateWrong(false.toString)
        result shouldBe Left(errorHeader + lineBreak + errorRow1 + lineBreak + errorRow2)
      }
    }

    Scenario("externalId not existing -> fail") {

      // prepare
      val user1 = DefaultModels.user()
      val user2 = DefaultModels.user()
      val activations =
        ActivationUpdate(Seq(
          UserActivationUpdate(user1.externalId, activate = true),
          UserActivationUpdate(user2.externalId, activate = true),
          UserActivationUpdate(UUID.randomString, activate = true)))

      for {
        //test
        result <-  userLogic.validate(activations, Seq(user1, user2))
      } yield {
        //verify
        result shouldBe Left(errorHeader + lineBreak +  activations.updates.last.toErrorCsv + extIdNotExisting)
      }
    }

  }

  Feature("updateActivation()") {

    Scenario("with instant de- and activation -> success") {

      // prepare
      val user1 = DefaultModels.user()
      val user2 = DefaultModels.user().copy(activeUser = Some(true))
      val activations =
        ActivationUpdate(Seq(
          UserActivationUpdate(user1.externalId, activate = true),
          UserActivationUpdate(user2.externalId, activate = false)))

      for {
        u1 <- UserManager.create(user1)
        u2 <- UserManager.create(user2)
        u1Before <- UserManager.findById(u1.get.id)
        u2Before <- UserManager.findById(u2.get.id)
        result <-  userLogic.updateActivation(activations)
        u1After <-  UserManager.findById(u1.get.id)
        u2After <-  UserManager.findById(u2.get.id)
      } yield {
        Seq(u1Before, u2Before, u1After, u2After).forall(_.isDefined) shouldBe true
        u1Before.get.activeUser shouldBe Some(false)
        u2Before.get.activeUser shouldBe Some(true)
        result match {
          case Right(msg) =>
            val expectedRows = header +: activations.updates.map(_.toCSV(None))
            expectedRows.map(row => msg.contains(row) shouldBe true)
          case _  => fail("result should be right")
        }
        u1After.get.activeUser shouldBe Some(true)
        u2After.get.activeUser shouldBe Some(false)
      }
    }

    Scenario("with scheduled user de- and activation -> success") {

      // prepare
      val tomorrow = Some(DateUtil.nowUTC.plusDays(1))
      val user1 = DefaultModels.user()
      val user2 = DefaultModels.user().copy(activeUser = Some(true), action = Some(Deactivate), executionDate = Some(DateUtil.nowUTC))
      val activations =
        ActivationUpdate(Seq(
          UserActivationUpdate(user1.externalId, activate = true, tomorrow),
          UserActivationUpdate(user2.externalId, activate = false, tomorrow)))

      for {
        u1 <- UserManager.create(user1)
        u2 <- UserManager.create(user2)
        result <-  userLogic.updateActivation(activations)
        u1After <-  UserManager.findById(u1.get.id)
        u2After <-  UserManager.findById(u2.get.id)
      } yield {
        Seq(u1After, u2After).forall(_.isDefined) shouldBe true
        result match {
          case Right(msg) =>
            val row1 = activations.updates.head.toCSV(None)
            val row2 = activations.updates.last.toCSV(user2.executionDate)
            Seq(header, row1 , row2).forall(msg.contains) shouldBe true
          case _  => fail("result should be right")
        }
        u1After shouldBe Some(user1.copy(action = Some(Activate), executionDate = tomorrow, updated = u1After.get.updated))
        u2After shouldBe Some(user2.copy(action = Some(Deactivate), executionDate = tomorrow, updated = u2After.get.updated))
      }
    }

    Scenario("removing old executionTime and action if updated instantly") {

      // prepare
      val tomorrow = Some(DateUtil.nowUTC.plusDays(1))
      val user1 = DefaultModels.user().copy(action = Some(Activate), executionDate = tomorrow)
      val user2 = DefaultModels.user().copy(action = Some(Activate), executionDate = tomorrow)
      val activations =
        ActivationUpdate(Seq(
          UserActivationUpdate(user1.externalId, activate = true),
          UserActivationUpdate(user2.externalId, activate = true)))

      for {
        u1 <- UserManager.create(user1)
        u2 <- UserManager.create(user2)
        result <-  userLogic.updateActivation(activations)
        u1After <-  UserManager.findById(u1.get.id)
        u2After <-  UserManager.findById(u2.get.id)
      } yield {
        Seq(u1After, u2After).forall(_.isDefined) shouldBe true
        result match {
          case Right(msg) =>
            val row1 = activations.updates.head.toCSV(user1.executionDate)
            val row2 = activations.updates.head.toCSV(user2.executionDate)
            Seq(header, row1 , row2).map(row => msg.contains(row) shouldBe true)
          case _  => fail("result should be right")
        }
        u1After shouldBe Some(user1.copy(activeUser = Some(true),action = None, executionDate = None, updated = u1After.get.updated))
        u2After shouldBe Some(user2.copy(activeUser = Some(true),action = None, executionDate = None, updated = u2After.get.updated))
      }
    }

  }
}
