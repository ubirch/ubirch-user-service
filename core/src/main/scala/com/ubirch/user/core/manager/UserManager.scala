package com.ubirch.user.core.manager

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.crypto.hash.HashUtil
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
        val errMsg = s"unable to create user as it's id already exist: user=$user"
        logger.error(errMsg)
        throw new Exception(errMsg)

      case None =>

        mongo.collection(collectionName) flatMap { collection =>

          // TODO update tests to include the Config.providersWithUsersActivated.contains() check
          logger.debug(s"create(): user.providerId=${user.providerId}")
          val userToCreate = (Config.providersWithUsersActivated.contains(user.providerId) match {
            case true => user.copy(activeUser = Some(true))
            case false => user
          }) match {
            case pu if pu.email.isDefined =>
              fixEmail(pu)
            case pu => pu.copy(
              hashedEmail = None
            )
          }
          validateUser(userToCreate)
          collection.insert[User](userToCreate) map { writeResult =>

            if (writeResult.ok && writeResult.n == 1) {
              logger.debug(s"created new user: $userToCreate")
              Some(userToCreate)
            } else {
              throw new Exception("failed to create user")
            }

          }

        }
    }

  }

  def update(user: User)(implicit mongo: MongoUtil): Future[Option[User]] = {

    val userId = user.id
    findById(userId) flatMap {

      case None =>
        val errMsg = s"unable to update if no User exists: userId=$userId"
        logger.error(errMsg)
        throw new Exception(errMsg)

      case Some(_: User) =>

        val patchedUser = fixEmail(user)
        validateUser(patchedUser)
        val selector = document("id" -> user.id)
        mongo.collection(collectionName) flatMap {

          _.update(selector, patchedUser) map { writeResult =>

            if (writeResult.ok) {
              logger.info(s"updated user: id=${user.id}")
              Some(patchedUser)
            } else {
              logger.error(s"failed to update user: user=$patchedUser, writeResult=$writeResult")
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

  def findByExternalId(externalId: String)
                      (implicit mongo: MongoUtil): Future[Option[User]] = {

    val selector = document(
      "externalId" -> cleanExternalId(externalId)
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

  private def validateUser(user: User): Unit = {
    if (user.email.isDefined && !checkMail(user.email))
      throw new Exception(s"invalid email: ${user.email.getOrElse("")}")
  }

  private def checkMail(email: Option[String]): Boolean = {
    email.isDefined &&
      email.get.length >= 6 &&
      !email.get.startsWith("@") &&
      email.get.contains("@") &&
      !email.get.endsWith(".") &&
      email.get.contains(".")
  }

  private def cleanExternalId(externalId: String): String = {
    externalId.toLowerCase.trim
  }

  private def fixEmail(user: User): User = {
    if (user.email.isDefined) {
      val cleanedEmail: String = cleanExternalId(user.email.get)
      val hashedEmail = HashUtil.sha512HexString(cleanedEmail)
      user.copy(
        email = Some(cleanedEmail),
        hashedEmail = Some(hashedEmail)
      )
    }
    else
      user.copy(
        email = None,
        hashedEmail = None
      )
  }

}
