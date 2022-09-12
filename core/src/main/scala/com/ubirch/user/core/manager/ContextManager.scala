package com.ubirch.user.core.manager

import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.core.manager.util.DBException.handleError
import com.ubirch.user.model.db.Context
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocumentHandler, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object ContextManager extends StrictLogging with MongoFormats {

  private val collectionName = Config.mongoCollectionContext
  private implicit val log = logger

  implicit protected def contextHandler: BSONDocumentHandler[Context] = Macros.handler[Context]

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
        val update = contextHandler.writeTry(context) match {
          case Success(doc) => doc
          case Failure(ex) => handleError(s"parse context $context to BSONDocument", ex)
        }

        mongo.collection(collectionName).flatMap {

          _.update(ordered = false).one(selector, update) map { writeResult =>
            if (writeResult.n == 1) {
              logger.info(s"updated context: id=$contextId")
              Some(context)
            } else {
              handleError(s"update context: context=$context, writeResult=$writeResult")
            }

          }

        }.recover(handleError(s"update context $context", _))

    }

  }

  def findById(id: String)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName).flatMap {
      _.find(selector).one[Context]
    }.recover(handleError(s"find by id $id", _))

  }

  def findByName(name: String)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    val selector = document("displayName" -> name)

    mongo.collection(collectionName).flatMap {
      _.find(selector).one[Context]
    }.recover(handleError(s"find by name $name", _))

  }

  def delete(id: String)(implicit mongo: MongoUtil): Future[Boolean] = {

    val selector = document("id" -> id)

    mongo.collection(collectionName).flatMap {
      _.delete().one(selector) map { writeResult =>
        if (writeResult.n == 1) {
          logger.info(s"deleted context: id=$id")
          true
        } else {
          logger.error(s"failed to delete context: id=$id (writeResult=$writeResult)")
          false
        }
      }
    }.recover(handleError(s"delete by id $id", _))

  }

  private def executeInsert(
                             findById: Option[Context],
                             findByName: Option[Context],
                             collection: BSONCollection,
                             context: Context): Future[Option[Context]] = {

    if (findById.isDefined || findByName.isDefined) {
      logger.error(s"unable to create context as it's displayName and/or id already exist: context=$context")
      Future(None)
    } else {

      collection.insert(ordered = false).one[Context](context).map { writeResult =>
        if (writeResult.n == 1) {
          logger.debug(s"created new context: $context")
          Some(context)
        } else {
          handleError(s"create context $context; writeResult=$writeResult")
        }

      }.recover(handleError(s"insert $context", _))

    }

  }
}
