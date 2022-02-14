package com.ubirch.user.testTools.db.mongo

import com.ubirch.util.mongo.test.MongoTestUtils
import org.scalatest.featurespec.AsyncFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}

/**
  * author: cvandrei
  * since: 2017-04-05
  */
class MongoSpec extends AsyncFeatureSpec
  with Matchers
  with MongoStorageCleanup
  with BeforeAndAfterEach
  with BeforeAndAfterAll {

  protected val mongoTestUtils = new MongoTestUtils()

  override protected def afterAll(): Unit = {
    cleanMongoDb()
  }

}
