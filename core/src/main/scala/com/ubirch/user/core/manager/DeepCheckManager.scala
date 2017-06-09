package com.ubirch.user.core.manager

import com.ubirch.user.config.Config
import com.ubirch.user.model.db.Context
import com.ubirch.util.deepCheck.model.DeepCheckResponse
import com.ubirch.util.mongo.connection.MongoUtil
import com.ubirch.util.mongo.format.MongoFormats

import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-06-08
  */
object DeepCheckManager extends MongoFormats {

  implicit protected def contextWriter: BSONDocumentWriter[Context] = Macros.writer[Context]

  implicit protected def contextReader: BSONDocumentReader[Context] = Macros.reader[Context]

  /**
    * Check if we can run a simple query on the database.
    *
    * @param mongo mongo connection wrapper
    * @return deep check response with _status:OK_ if ok; otherwise with _status:NOK_
    */
  def connectivityCheck()(implicit mongo: MongoUtil): Future[DeepCheckResponse] = {

    val collectionName = Config.mongoCollectionContext
    mongo.connectivityCheck[Context](collectionName)

  }

}
