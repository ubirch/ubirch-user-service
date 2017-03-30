package com.ubirch.user.core.manager

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.rest.User
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object UserManager extends StrictLogging {

  def create(userRest: User): Future[User] = {

    // TODO implement
    Future(userRest.copy(id = Some(UUIDUtil.uuid)))

  }

  def update(userRest: User): Future[User] = {

    // TODO implement
    Future(userRest)

  }

  def findByProviderIdExternalId(providerId: String, externalUserId: String): Future[User] = {

    // TODO implement
    Future(
      User(
        id = Some(UUIDUtil.uuid),
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
        id = Some(id),
        displayName = "displayName-find",
        providerId = "some-provider-id",
        externalId = "some-external-id"
      )
    )

  }

}
