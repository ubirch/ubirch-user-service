package com.ubirch.user.core.manager

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Group
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object GroupManager extends StrictLogging
  with MongoFormats {

  private val collectionName = Config.mongoCollectionGroup

  implicit protected def groupWriter: BSONDocumentWriter[Group] = Macros.writer[Group]

  implicit protected def groupReader: BSONDocumentReader[Group] = Macros.reader[Group]

  def create(group: Group)(implicit mongo: MongoUtil): Future[Option[Group]] = {

    findById(group.id) flatMap {

      case None =>

        mongo.collection(collectionName) flatMap { collection =>

          collection.insert(ordered = false).one[Group](group) map { writeResult =>

            if (writeResult.ok && writeResult.n == 1) {
              logger.debug(s"created new group: $group")
              Some(group)
            } else {
              logger.error("failed to create group")
              None
            }
          }

        }

      case Some(_: Group) =>
        logger.error("unable to create group that already exists")
        Future(None)

    }

  }

  def update(group: Group)(implicit mongo: MongoUtil): Future[Option[Group]] = {

    val groupId = group.id
    findById(groupId) flatMap {

      case None =>
        logger.error(s"unable to update if no Group exists: groupId=$groupId")
        Future(None)

      case Some(_: Group) =>

        val selector = document("id" -> groupId)
        mongo.collection(collectionName) flatMap {

          _.update(ordered = false).one(selector, group) map { writeResult =>

            if (writeResult.ok) {
              logger.info(s"updated group: id=$groupId")
              Some(group)
            } else {
              logger.error(s"failed to update group: group=$group, writeResult=$writeResult")
              None
            }

          }

        }

    }

  }

  def findById(id: String)(implicit mongo: MongoUtil): Future[Option[Group]] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName) flatMap {
      _.find[BSONDocument, Group](selector).one[Group]
    }

  }

  def findByContextAndOwner(contextId: String, ownerId: String)(implicit mongo: MongoUtil): Future[Option[Group]] = {

    // TODO automated tests
    val selector = document("contextId" -> contextId, "ownerIds" -> ownerId)

    mongo.collection(collectionName) flatMap {
      _.find[BSONDocument, Group](selector).one[Group]
    }

  }

  def delete(id: String)(implicit mongo: MongoUtil): Future[Boolean] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName) flatMap {
      _.delete().one(selector) map { writeResult =>

        if (writeResult.ok && writeResult.n == 1) {
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
