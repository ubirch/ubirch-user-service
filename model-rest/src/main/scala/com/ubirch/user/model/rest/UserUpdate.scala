package com.ubirch.user.model.rest

case class UserUpdate(displayName: String)

case class UpdateInfo(simpleUserContext: SimpleUserContext, update: UserUpdate)
