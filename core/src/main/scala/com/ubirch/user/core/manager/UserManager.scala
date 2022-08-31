package com.ubirch.user.core.manager

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.model.db.{Action, User}
import com.ubirch.util.crypto.hash.HashUtil
import com.ubirch.util.date.DateUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats
import reactivemongo.bson.{BSON, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONHandler, BSONString, Macros, document}
import org.joda.time.DateTime
import reactivemongo.api.Cursor

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object UserManager extends StrictLogging
  with MongoFormats {

  private val collectionName = Config.mongoCollectionUser

  implicit protected object BSONActionHandler extends BSONHandler[BSONString, Action] {
    def read(action: BSONString): Action = Action.unsafeFromString(action.value)

    def write(action: Action): BSONString = BSONString(Action.toFormattedString(action))
  }

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
          val userToCreate = (
            if (Config.providersWithUsersActivated.contains(user.providerId)) user.copy(activeUser = Some(true))
            else user
          ) match {
            case pu if pu.email.isDefined =>
              fixEmail(pu)
            case pu => pu.copy(
              hashedEmail = None
            )
          }
          validateUser(userToCreate)
          val withExternalIdLowerCase = userToCreate.copy(externalId = userToCreate.externalId.toLowerCase)
          collection
            .insert(ordered = false)
            .one[User](withExternalIdLowerCase)
            .map { writeResult =>
              if (writeResult.ok && writeResult.n == 1) {
                logger.debug(s"created new user: $userToCreate")
                Some(withExternalIdLowerCase)
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
        val userToUpdate = patchedUser.copy(updated = DateUtil.nowUTC)
        validateUser(patchedUser)
        val selector = document("id" -> user.id)
        mongo.collection(collectionName) flatMap {

          _.update(ordered = false).one(selector, userToUpdate) map { writeResult =>

            if (writeResult.ok) {
              logger.info(s"updated user: id=${user.id}")
              Some(userToUpdate)
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
      _.find[BSONDocument, User](selector).one[User]
    }

  }

  def findByProviderIdAndExternalId(providerId: String, externalUserId: String)
                                   (implicit mongo: MongoUtil): Future[Option[User]] = {

    val selector = document(
      "providerId" -> providerId,
      "externalId" -> externalUserId.toLowerCase
    )

    mongo.collection(collectionName) flatMap {
      _.find[BSONDocument, User](selector).one[User]
    }

  }


  def updateMany(users: Seq[User])(implicit mongo: MongoUtil): Future[Either[String, Seq[User]]] = {

    val usersWithLastUpdated = users.map(_.copy(updated = DateUtil.nowUTC))

    mongo.collection(collectionName).flatMap { coll =>
      val updateBuilder = coll.update(ordered = false)
      val userUpdates =
        Future.sequence(
          usersWithLastUpdated
            .map(user =>
              updateBuilder
                .element(
                  q = document("id" -> user.id),
                  u = BSON.writeDocument[User](user))))

      userUpdates
        .flatMap(ops => updateBuilder.many(ops))
        .map { writeResult =>
          if (writeResult.ok && writeResult.n == users.size) {
            Right(usersWithLastUpdated)
          } else {
            val errorMsg = s"error on updating users in mongoDB with ids ${users.map(_.id)}"
            logger.error(errorMsg + s" with writeResult $writeResult")
            Left(errorMsg)
          }
        }
    }
  }

  def findByExternalIds(externalIds: Seq[String])
                       (implicit mongo: MongoUtil): Future[Seq[User]] = {

    val selector = document("externalId" -> document("$in" -> externalIds.map(cleanExternalId)))

    mongo
      .collection(collectionName)
      .flatMap { collection =>
        collection
          .find[BSONDocument, User](selector)
          .cursor[User]()
          .collect[Seq](
            -1,
            Cursor.FailOnError[Seq[User]]()
          )
      }
      .recover {
        case ex =>
          logger.error(s"error when retrieving users with the following externalIds $externalIds: ${ex.getMessage}")
          Seq[User]()
      }
  }

  def findByUsedIds(userIds: Seq[UUID])
                       (implicit mongo: MongoUtil): Future[Seq[User]] = {

    val selector = document("id" -> document("$in" -> userIds.map(_.toString)))

    mongo
      .collection(collectionName)
      .flatMap { collection =>
        collection
          .find[BSONDocument, User](selector)
          .cursor[User]()
          .collect[Seq](
            -1,
            Cursor.FailOnError[Seq[User]]()
          )
      }
      .recover {
        case ex =>
          logger.error(s"error when retrieving users with the following userIds $userIds: ${ex.getMessage}")
          Seq[User]()
      }
  }

  def findByExternalId(externalId: String)
                      (implicit mongo: MongoUtil): Future[Option[User]] = {

    val selector = document(
      "externalId" -> cleanExternalId(externalId)
    )

    mongo.collection(collectionName) flatMap {
      _.find[BSONDocument, User](selector).one[User]
    }

  }

  def delete(id: String)(implicit mongo: MongoUtil): Future[Boolean] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName) flatMap {
      _.delete().one(selector) map { writeResult =>

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

  def getWithOffset(limit: Int, offset: Option[Int])(implicit mongo: MongoUtil): Future[List[User]] = {
    val skipNumOfUsers = limit * offset.getOrElse(0)
    val sort = document("created" -> 1)
    mongo.collection(collectionName) flatMap {
      _.find(document(), None).sort(sort).skip(skipNumOfUsers).cursor[User]().collect[List](limit, Cursor.FailOnError[List[User]]())
    }
  }

  def getWithCursor(limit: Int, lastCreatedAt: Option[DateTime])(implicit mongo: MongoUtil): Future[List[User]] = {
    val selector = lastCreatedAt match {
      case Some(createdAt) => document("created" -> document("$gt" -> createdAt))
      case None => document()
    }
    val sort = document("created" -> 1)
    mongo.collection(collectionName) flatMap {
      _.find(selector, None).sort(sort).cursor[User]().collect[List](limit, Cursor.FailOnError[List[User]]())
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
