package com.ubirch.user.core.manager

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.User
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object UserManager extends StrictLogging
  with MongoFormats {

  private val collectionName = Config.mongoCollectionUser

  implicit protected def userWriter: BSONDocumentWriter[User] = Macros.writer[User]

  implicit protected def userReader: BSONDocumentReader[User] = Macros.reader[User]

  def create(user: User)(implicit mongo: MongoUtil): Future[Option[User]] = {

    findByProviderIdAndExternalId(providerId = user.providerId, externalUserId = user.externalId) flatMap {

      case Some(_: User) =>

        logger.error(s"unable to create user as it's id already exist: user=$user")
        Future(None)

      case None =>

        mongo.collection(collectionName) flatMap { collection =>

          collection.insert[User](user) map { writeResult =>

            if (writeResult.ok && writeResult.n == 1) {
              logger.debug(s"created new user: $user")
              Some(user)
            } else {
              logger.error("failed to create user")
              None
            }

          }

        }

    }

  }

  def update(user: User)(implicit mongo: MongoUtil): Future[Option[User]] = {

    val userId = user.id
    findById(userId) flatMap {

      case None =>
        logger.error(s"unable to update if no User exists: userId=$userId")
        Future(None)

      case Some(_: User) =>

        val selector = document("id" -> user.id)
        mongo.collection(collectionName) flatMap {

          _.update(selector, user) map { writeResult =>

            if (writeResult.ok) {
              logger.info(s"updated user: id=${user.id}")
              Some(user)
            } else {
              logger.error(s"failed to update user: user=$user, writeResult=$writeResult")
              None
            }

          }

        }

    }

  }

  def findById(id: String)(implicit mongo: MongoUtil): Future[Option[User]] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName) flatMap {
      _.find(selector).one[User]
    }

  }

  def findByProviderIdAndExternalId(providerId: String, externalUserId: String)
                                   (implicit mongo: MongoUtil): Future[Option[User]] = {

    val selector = document(
      "providerId" -> providerId,
      "externalId" -> externalUserId
    )

    mongo.collection(collectionName) flatMap {
      _.find(selector).one[User]
    }

  }

  def delete(id: String)(implicit mongo: MongoUtil): Future[Boolean] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName) flatMap {
      _.remove(selector) map { writeResult =>

        if (writeResult.ok && writeResult.n == 1) {
          logger.info(s"deleted user: id=$id")
          true
        } else {
          logger.error(s"failed to delete user: id=$id (writeResult=$writeResult)")
          false
        }

      }
    }

  }

}
