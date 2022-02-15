package com.ubirch.user.core.actor

import com.ubirch.user.core.manager.UserManager
import com.ubirch.user.model.db.{Activate, Deactivate, User}
import com.ubirch.user.model.rest.ActivationUpdate
import com.ubirch.user.model.rest.ActivationUpdate._
import com.ubirch.util.date.DateUtil
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserActorLogic {

  protected[actor] def updateActivation(updates: ActivationUpdate)(implicit mongo: MongoUtil): Future[Either[String, String]] = {

    val activationMap = updates.updates.map(u => u.externalId -> (u.activate, u.executionDate)).toMap

    for {
      users <- UserManager.findByExternalIds(activationMap.keys.toSeq)
      result <- validate(updates, users).flatMap {
        case Left(errorCsv) => Future.successful(Left(errorCsv))
        case _ => updateUsers(updates, users).map(successMsg => Right(header + lineBreak + successMsg.mkString(lineBreak)))
      }
    } yield result

  }

  private[actor] def validate(update: ActivationUpdate, users: Seq[User]): Future[Either[String, Unit]] = {

    val now = DateUtil.nowUTC

    val errors = update.updates.map { u =>
      if (u.executionDate.isDefined && u.executionDate.get.isBefore(now))
        Some(u.toErrorCsv + dateInPast)
      else
        users.find(_.externalId == u.externalId) match {
          case Some(user) if user.activeUser.contains(u.activate) =>
            Some(u.toErrorCsv + targetStateWrong(user.activeUser.get))
          case None => Some(u.toErrorCsv + extIdNotExisting)
          case _ => None
        }
    }.collect { case Some(el) => el }
    if (errors.nonEmpty) Future.successful(Left(errorHeader + lineBreak + errors.mkString(lineBreak)))
    else Future.successful(Right(()))
  }

  private def updateUsers(updates: ActivationUpdate, users: Seq[User])(implicit mongo: MongoUtil): Future[Seq[String]] = {

    val usersAndMessages = users.map { user =>
      updates.updates.find(_.externalId == user.externalId) match {

        case Some(update) if update.executionDate.isDefined =>
          val action = if (update.activate) Activate else Deactivate
          val updated = user.copy(action = Some(action),  executionDate = update.executionDate)
          val successMsg = update.toCSV(user.executionDate)
          (updated, successMsg)

        case Some(update) =>
          val updated = user.copy(activeUser = Some(update.activate), executionDate = None, action = None)
          val successMsg = update.toCSV(user.executionDate)
          (updated, successMsg)

        case None => throw new Exception("update not found; should be excluded by previous validation")
      }
    }
    UserManager
      .updateMany(usersAndMessages.map(_._1))
      .map {
        case Right(_) => usersAndMessages.map(_._2)
        case Left(errorMsg) => throw new Exception(errorMsg)
      }
  }

}
