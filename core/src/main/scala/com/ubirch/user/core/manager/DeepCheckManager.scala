package com.ubirch.user.core.manager

import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Context
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.deepCheck.util.DeepCheckResponseUtil
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-08
  */
object DeepCheckManager
  extends MongoFormats
    with StrictLogging {

  implicit protected def contextWriter: BSONDocumentWriter[Context] = Macros.writer[Context]

  implicit protected def contextReader: BSONDocumentReader[Context] = Macros.reader[Context]

  /**
    * Check if we can run a simple query on the database.
    *
    * @param mongo mongo connection wrapper
    * @return deep check response with _status:true if ok; otherwise with _status:false_
    */
  def connectivityCheck()(implicit mongo: MongoUtil): Future[DeepCheckResponse] = {

    val testState = try {
      mongo.checkConnection()
    }
    catch {
      case e: Exception =>
        logger.error(s"mongo check failed: ${e.getMessage}")
        false
    }

    if (testState) {
      val collectionName = Config.mongoCollectionContext
      mongo.connectivityCheck[Context](collectionName).map { deepCheckRes =>
        DeepCheckResponseUtil.addServicePrefix("user-service", deepCheckRes)
      }
    }
    else
      mongo.db.flatMap { db =>
        db.serverStatus.map { sStat =>
          val host = sStat.host
          val mgName = db.connection.name
          DeepCheckResponse(
            status = false,
            messages = Seq(s"no db connection ($host / $mgName)")
          )
        }

      }
  }
}
