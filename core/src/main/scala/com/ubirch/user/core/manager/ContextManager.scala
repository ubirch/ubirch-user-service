package com.ubirch.user.core.manager

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Context
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object ContextManager extends StrictLogging
  with MongoFormats {

  implicit def contextWriter: BSONDocumentWriter[Context] = Macros.writer[Context]
  implicit def contextReader: BSONDocumentReader[Context] = Macros.reader[Context]

  def create(context: Context)(implicit mongo: MongoUtil): Future[Context] = {

    mongo.collection(Config.mongoCollectionContext) map { collection =>

      // TODO if context already exists
      collection.insert[Context](context) onComplete {

        case Failure(e) =>
          logger.error("failed to create context", e)
          throw e

        case Success(_) => logger.info(s"created new context: $context")

      }
      context

    }

  }

  def update(context: Context)(implicit mongo: MongoUtil): Future[Context] = {

    // TODO implement
    printCollectionNames()
    Future(context)

  }

  def get(id: UUID)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    // TODO implement
    printCollectionNames()
    Future(Some(Context(id, "foo-display-name-get")))

  }

  def findByName(name: String)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    // TODO implement
    printCollectionNames()
    Future(Some(Context(displayName = name)))

  }

  def delete(id: UUID)(implicit mongo: MongoUtil): Future[Context] = {

    // TODO implement
    printCollectionNames()
    Future(Context(id, "foo-display-name-delete"))

  }

  private def printCollectionNames()(implicit mongo: MongoUtil): Unit = {

    mongo.db() map { db =>

      logger.info(s"connected to database: ${db.name}")
      logger.info("listing collection names")
      db.collectionNames map println

    }

  }

}
