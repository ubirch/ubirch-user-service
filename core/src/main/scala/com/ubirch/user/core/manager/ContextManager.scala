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

    mongo.collection(Config.mongoCollectionContext) flatMap { collection =>

      for {
        findById <- findById(context.id)
        findByName <- findByName(context.displayName)
      } yield {

        if (findById.isDefined || findByName.isDefined) {

          logger.error(s"unable to create context as it's displayName and/or id already exist: context=$context")
          None

        } else {

          collection.insert[Context](context) onComplete {

            case Failure(e) =>
              logger.error("failed to create context", e)
              throw e

            case Success(_) => logger.info(s"created new context: $context")

          }
          Some(context)

        }

      }

    }

  }

  def update(context: Context)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    val selector = document("id" -> context.id)
    val update = contextWriter.write(context)

    mongo.collection(Config.mongoCollectionContext) flatMap {

      _.update(selector, update) map { writeResult =>

        if (writeResult.ok && writeResult.n == 1) {
          logger.info(s"updated context: id=${context.id}")
          Some(context)
        } else {
          logger.error(s"failed to update context: context=$context")
          None
        }

      }

    }

  }

  def findById(id: UUID)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    val query = document("id" -> id)

    mongo.collection(Config.mongoCollectionContext) flatMap {
      _.find(query).one[Context]
    }

  }

  def findByName(name: String)(implicit mongo: MongoUtil): Future[Option[Context]] = {

    val query = document("displayName" -> name)

    mongo.collection(Config.mongoCollectionContext) flatMap {
      _.find(query).one[Context]
    }

  }

  def delete(id: UUID)(implicit mongo: MongoUtil): Future[Boolean] = {

    val selector = document("id" -> id)

    mongo.collection(Config.mongoCollectionContext) flatMap {
      _.remove(selector) map { writeResult =>

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

}
