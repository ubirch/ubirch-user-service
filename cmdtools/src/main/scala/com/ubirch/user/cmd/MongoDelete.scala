package com.ubirch.user.cmd

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.config.ConfigKeys
import com.ubirch.util.mongo.connection.MongoUtil

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * author: cvandrei
  * since: 2017-04-07
  */
object MongoDelete extends App
  with StrictLogging {

  private implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  // TODO migrate to encapsulate all executable logic within a method `run(): Unit`
  Await.result(mongo.db map(_.drop), 60 seconds)
  mongo.close()

}
