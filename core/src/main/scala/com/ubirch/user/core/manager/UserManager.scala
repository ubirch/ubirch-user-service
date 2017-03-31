package com.ubirch.user.core.manager

import java.util.UUID

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

  def update(user: User): Future[User] = {

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

  def delete(id: UUID): Future[User] = {

    // TODO implement
    Future(
      User(
        displayName = "displayName-find",
        providerId = "some-provider-id",
        externalId = "some-external-id"
      )
    )

  }

}
