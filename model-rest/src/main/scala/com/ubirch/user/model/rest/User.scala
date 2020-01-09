package com.ubirch.user.model.rest

import java.util.UUID

import org.joda.time.DateTime

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class User(id: Option[UUID] = None,
                displayName: String,
                providerId: String,
                externalId: String,
                email: Option[String] = None,
                activeUser: Boolean = false,
                locale: String,
                created: DateTime = DateTime.now(),
                updated: DateTime = DateTime.now())
