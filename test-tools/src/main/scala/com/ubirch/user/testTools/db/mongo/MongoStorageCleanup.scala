package com.ubirch.user.testTools.db.mongo

import com.ubirch.user.config.ConfigKeys
import com.ubirch.user.util.mongo.UserMongoConstraints
import com.ubirch.util.mongo.connection.MongoUtil
import reactivemongo.api.DefaultDB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
  * author: cvandrei
  * since: 2018-04-04
  */
trait MongoStorageCleanup extends UserMongoConstraints {

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)
  implicit val connection: Future[DefaultDB] = mongo.db

  final def mongoClose(): Unit = mongo.close()

  final def dropMongoDb(): Unit = {
    Await.result(connection.map(_.drop()), 60.seconds)
    createMongoConstraints()
    logger.info("dropped mongo database")
  }

  final def cleanMongoDb(): Unit = {
    val r = Future.sequence(collections.map(c => mongo.collection(c).flatMap(_.drop(failIfNotFound = false))))
    Await.result(r, 30.seconds)
    createMongoConstraints()
    logger.info(s"dropped mongo documents")
  }

}
