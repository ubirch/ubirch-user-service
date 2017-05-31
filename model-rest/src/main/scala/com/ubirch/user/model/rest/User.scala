package com.ubirch.user.model.rest

import java.util.UUID

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class User(id: Option[UUID] = None,
                displayName: String,
                providerId: String,
                externalId: String,
                activeUser: Boolean,
                locale: String
               )
