package com.ubirch.user.core.manager

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.db.User

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object UserManager extends StrictLogging {

  def create(user: User): Future[User] = {

    // TODO implement
    Future(user)

  }

  def update(providerId: String,
             externalUserId: String,
             user: User
            ): Future[User] = {

    // TODO implement
    Future(user)

  }

  def findByProviderIdExternalId(providerId: String, externalUserId: String): Future[User] = {

    // TODO implement
    Future(
      User(
        displayName = "displayName-find",
        providerId = providerId,
        externalId = externalUserId
      )
    )

  }

  def delete(providerId: String, externalUserId: String): Future[User] = {

    // TODO implement
    Future(
      User(
        displayName = "displayName-delete",
        providerId = providerId,
        externalId = externalUserId
      )
    )

  }

}
