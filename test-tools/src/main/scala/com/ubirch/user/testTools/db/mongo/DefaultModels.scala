package com.ubirch.user.testTools.db.mongo

import com.ubirch.user.model.db.{Context, User}

import scala.util.Random

/**
  * author: cvandrei
  * since: 2017-04-06
  */
object DefaultModels {

  def context(displayName: String = "automated-test"): Context = {

    Context(
      displayName = displayName
    )

  }

  def user(displayName: String = "automated-test-user",
           providerId: String = "google",
           externalId: String = Random.nextInt().toString
          ): User = {

    User(
      displayName = displayName,
      providerId = providerId,
      externalId = externalId
    )

  }

}
