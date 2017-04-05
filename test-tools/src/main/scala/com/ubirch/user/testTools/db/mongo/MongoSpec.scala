package com.ubirch.user.testTools.db.mongo

import com.ubirch.user.config.ConfigKeys
import com.ubirch.util.mongo.connection.MongoUtil

import org.scalatest.{AsyncFeatureSpec, BeforeAndAfterAll, BeforeAndAfterEach, Matchers}

/**
  * author: cvandrei
  * since: 2017-04-05
  */
class MongoSpec extends AsyncFeatureSpec
  with Matchers
  with BeforeAndAfterEach
  with BeforeAndAfterAll {

  protected implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  override protected def beforeEach(): Unit = {
    mongo.db() map(_.drop())
    Thread.sleep(100)
  }

  override protected def afterAll(): Unit = mongo.close()

}
