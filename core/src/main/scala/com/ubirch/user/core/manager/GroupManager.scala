package com.ubirch.user.core.manager

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.util.DBException.handleError
import com.ubirch.user.model.db.Group
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats
import reactivemongo.api.bson.{BSONDocumentHandler, Macros, document}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object GroupManager extends StrictLogging with MongoFormats {

  private val collectionName = Config.mongoCollectionGroup
  implicit private val log = logger

  implicit protected def groupHandler: BSONDocumentHandler[Group] = Macros.handler[Group]

  def create(group: Group)(implicit mongo: MongoUtil): Future[Option[Group]] = {

    findById(group.id) flatMap {

      case None =>
        mongo.collection(collectionName) flatMap { collection =>
          collection.insert(ordered = false).one[Group](group) map { writeResult =>
            if (writeResult.n == 1) {
              logger.debug(s"created new group: $group")
              Some(group)
            } else handleError("failed to create group")
          }
        }
      case Some(_: Group) =>
        handleError("unable to create group that already exists")

    }

  }

  def update(group: Group)(implicit mongo: MongoUtil): Future[Option[Group]] = {

    val groupId = group.id
    findById(groupId) flatMap {

      case None =>
        handleError(s"update if no Group exists: groupId=$groupId")

      case Some(_: Group) =>
        val selector = document("id" -> groupId)
        mongo.collection(collectionName).flatMap {

          _.update(ordered = false).one(selector, group) map { writeResult =>
            if (writeResult.n == 1) {
              logger.info(s"updated group: id=$groupId")
              Some(group)
            } else handleError(s"failed to update group: group=$group, writeResult=$writeResult")

          }

        }.recover(handleError(s"update group $group", _))

    }

  }

  def findById(id: String)(implicit mongo: MongoUtil): Future[Option[Group]] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName).flatMap {
      _.find(selector).one[Group]
    }.recover(handleError(s"find by id $id", _))

  }

  def findByContextAndOwner(contextId: String, ownerId: String)(implicit mongo: MongoUtil): Future[Option[Group]] = {

    // TODO automated tests
    val selector = document("contextId" -> contextId, "ownerIds" -> ownerId)

    mongo.collection(collectionName).flatMap {
      _.find(selector).one[Group]
    }.recover(handleError(s"find by context $contextId and owner $ownerId", _))

  }

  def delete(id: String)(implicit mongo: MongoUtil): Future[Boolean] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName) flatMap {
      _.delete().one(selector) map { writeResult =>
        if (writeResult.n == 1) {
          logger.info(s"deleted group: id=$id")
          true
        } else {
          logger.error(s"failed to delete group: id=$id (writeResult=$writeResult)")
          false
        }

      }
    }

  }

  def addAllowedUsers(groupId: UUID, allowedUsers: Set[UUID]): Future[Boolean] = {

    // TODO implement
    Future(true)

  }

  def deleteAllowedUsers(groupId: UUID, allowedUsers: Set[UUID]): Future[Boolean] = {

    // TODO implement
    Future(true)

  }

}
