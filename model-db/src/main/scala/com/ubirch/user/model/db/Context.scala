package com.ubirch.user.model.db

import java.util.UUID

import com.ubirch.util.uuid.UUIDUtil

/**
  * author: cvandrei
  * since: 2017-03-29
  */
case class Context(id: UUID = UUIDUtil.uuid,
                   displayName: String
                  )
