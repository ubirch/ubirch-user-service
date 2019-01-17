package com.ubirch.user.core.manager

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.model.db.{Context, Group, User}
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats
import reactivemongo.api.Cursor
import reactivemongo.bson.{BSONArray, BSONDocumentReader, BSONDocumentWriter, Macros, document}

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

    for {

      userOpt <- UserManager.findByProviderIdAndExternalId(providerId = providerId, externalUserId = externalUserId)
      contextOpt <- ContextManager.findByName(contextName)
      groups <- findAllGroupsFuture(userOpt, contextOpt)

    } yield groups

  }

  /**
    * Finds all groups where the given user is the owner or is listed in allowedUsers_.
    *
    * @param userOpt    user being the owner
    * @param contextOpt context the groups exist in
    * @param mongo      database connection
    * @return all groups found; empty if none
    */
  private def findAllGroupsFuture(userOpt: Option[User], contextOpt: Option[Context])(implicit mongo: MongoUtil): Future[Set[Group]] = {

    if (userOpt.isDefined && contextOpt.isDefined) {

      mongo.collection(collectionName) flatMap {

        val userId = userOpt.get.id
        val selector = document(
          document("contextId" -> contextOpt.get.id),
          document("$or" ->
            BSONArray(
              document("ownerIds" ->
                document("$in" -> Set(userId))
              ),
              document("allowedUsers" ->
                document("$in" -> Set(userId))
              )
            )
          )
        )
        _.find(selector, None)
          .cursor[Group]()
          .collect[Set](
          -1,
          Cursor.FailOnError[Set[Group]]()
        )

      }

    } else {
      logger.info(s"user or context does not exist: user.isDefined=${userOpt.isDefined}, context.isDefined=${contextOpt.isDefined}")
      Future(Set.empty)
    }

  }

  /**
    * Finds only groups where the given user is the owner.
    *
    * @param userOpt    user being the owner
    * @param contextOpt context the groups exist in
    * @param mongo      database connection
    * @return all groups found; empty if none
    */
  private def findOwnGroupsFuture(userOpt: Option[User], contextOpt: Option[Context])(implicit mongo: MongoUtil): Future[Set[Group]] = {

    if (userOpt.isDefined && contextOpt.isDefined) {

      mongo.collection(collectionName) flatMap {

        val userId = userOpt.get.id
        val selector = document(
          document("contextId" -> contextOpt.get.id),
          document("ownerIds" ->
            document("$in" -> Set(userId))
          )
        )
        _.find(selector, None)
          .cursor[Group]()
          .collect[Set](
          -1,
          Cursor.FailOnError[Set[Group]]()
        )

      }

    } else {
      logger.error(s"user or context does not exist: user.isDefined=${userOpt.isDefined}, context.isDefined=${contextOpt.isDefined}")
      Future(Set.empty)
    }

  }

}
