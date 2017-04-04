package com.ubirch.user.core.manager

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.db.Context
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * author: cvandrei
  * since: 2017-03-30
  */
object ContextManager extends StrictLogging {

  def create(context: Context)(implicit mongo: MongoUtil): Future[Context] = {

    // TODO implement
    printCollectionNames()
    Future(context)

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
