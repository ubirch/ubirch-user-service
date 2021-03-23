package com.ubirch.user.core.manager

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Context
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object ContextManager extends StrictLogging
  with MongoFormats {

  private val collectionName = Config.mongoCollectionContext

  implicit protected def contextWriter: BSONDocumentWriter[Context] = Macros.writer[Context]

  implicit protected def contextReader: BSONDocumentReader[Context] = Macros.reader[Context]

  def create(context: Context)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    for {

      collection <- mongo.collection(collectionName)
      findById <- findById(context.id)
      findByName <- findByName(context.displayName)

      result <- executeInsert(findById, findByName, collection, context)

    } yield result

  }

  def update(context: Context)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    val contextId = context.id
    findById(contextId) flatMap {

      case None =>
        logger.error(s"unable to update if no Context exists: contextId=$contextId")
        Future(None)

      case Some(_: Context) =>

        val selector = document("id" -> contextId)
        val update = contextWriter.write(context)

        mongo.collection(collectionName) flatMap {

          _.update(ordered = false).one(selector, update) map { writeResult =>

            if (writeResult.ok) {
              logger.info(s"updated context: id=$contextId")
              Some(context)
            } else {
              logger.error(s"failed to update context: context=$context, writeResult=$writeResult")
              None
            }

          }

        }

    }

  }

  def findById(id: String)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName) flatMap {
      _.find[BSONDocument, Context](selector).one[Context]
    }

  }

  def findByName(name: String)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    val selector = document("displayName" -> name)

    mongo.collection(collectionName) flatMap {
      _.find[BSONDocument, Context](selector).one[Context]
    }

  }

  def delete(id: String)(implicit mongo: MongoUtil): Future[Boolean] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName) flatMap {
      _.delete().one(selector) map { writeResult =>

        if (writeResult.ok && writeResult.n == 1) {
          logger.info(s"deleted context: id=$id")
          true
        } else {
          logger.error(s"failed to delete context: id=$id (writeResult=$writeResult)")
          false
        }

      }
    }

  }

  private def executeInsert(findById: Option[Context],
                            findByName: Option[Context],
                            collection: BSONCollection,
                            context: Context
                           ): Future[Option[Context]] = {

    if (findById.isDefined || findByName.isDefined) {
      logger.error(s"unable to create context as it's displayName and/or id already exist: context=$context")
      Future(None)
    } else {

      collection.insert(ordered = false).one[Context](context) map { writeResult =>

        if (writeResult.ok && writeResult.n == 1) {
          logger.debug(s"created new context: $context")
          Some(context)
        } else {
          logger.error("failed to create context")
          None
        }

      }

    }

  }

}
