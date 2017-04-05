package com.ubirch.user.core.manager

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Context
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object ContextManager extends StrictLogging
  with MongoFormats {

  implicit protected def contextWriter: BSONDocumentWriter[Context] = Macros.writer[Context]

  implicit protected def contextReader: BSONDocumentReader[Context] = Macros.reader[Context]

  def create(context: Context)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    // TODO automated tests
    mongo.collection(Config.mongoCollectionContext) flatMap { collection =>

      findByName(context.displayName) map {

        case None =>

          collection.insert[Context](context) onComplete {

            case Failure(e) =>
              logger.error("failed to create context", e)
              throw e

            case Success(_) => logger.info(s"created new context: $context")

          }
          Some(context)

        case Some(_) => None

      }

    }

  }

  def update(context: Context)(implicit mongo: MongoUtil): Future[Context] = {

    // TODO implement
    printCollectionNames()
    Future(context)

  }

  def get(id: UUID)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    // TODO automated tests
    val query = document("id" -> id)

    mongo.collection(Config.mongoCollectionContext) flatMap {
      _.find(query).one[Context]
    }

  }

  def findByName(name: String)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    // TODO automated tests
    val query = document("displayName" -> name)

    mongo.collection(Config.mongoCollectionContext) flatMap {
      _.find(query).one[Context]
    }

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
