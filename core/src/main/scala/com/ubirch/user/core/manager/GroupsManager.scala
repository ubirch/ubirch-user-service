package com.ubirch.user.core.manager

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object GroupsManager extends StrictLogging
  with MongoFormats {

  private val collectionName = Config.mongoCollectionGroup

  implicit protected def groupWriter: BSONDocumentWriter[Group] = Macros.writer[Group]

  implicit protected def groupReader: BSONDocumentReader[Group] = Macros.reader[Group]

  def findByContextAndUser(contextName: String,
                           providerId: String,
                           externalUserId: String
                          )
                          (implicit mongo: MongoUtil): Future[Set[Group]] = {

    // TODO automated tests
    for {

      userOpt <- UserManager.findByProviderIdAndExternalId(providerId = providerId, externalUserId = externalUserId)
      contextOpt <- ContextManager.findByName(contextName)
      groups <- findGroupsFuture(userOpt, contextOpt)

    } yield groups

  }

  private def findGroupsFuture(userOpt: Option[User], contextOpt: Option[Context])(implicit mongo: MongoUtil): Future[Set[Group]] = {

    if (userOpt.isDefined && contextOpt.isDefined) {

      mongo.collection(collectionName) flatMap {

        val userId = userOpt.get.id
        val selector = document(
          document("contextId" -> contextOpt.get.id),
          document("$or" ->
            Set(
              document("ownerId" -> userId),
              document("allowedUsers" -> document("$all" -> Set(userId)))
            )
          )
        )
        _.find(selector)
          .cursor[Group]()
          .collect[Set]()

      }

    } else {
      logger.info(s"user or context does not exist: user.isDefined=${userOpt.isDefined}, context.isDefined=${contextOpt.isDefined}")
      Future(Set.empty)
    }

  }

}
