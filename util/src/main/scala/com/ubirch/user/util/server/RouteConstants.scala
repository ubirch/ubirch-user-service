package com.ubirch.user.util.server

import java.util.UUID

/**
  * author: cvandrei
  * since: 2017-03-22
  */
object RouteConstants {

  final val apiPrefix = "api"
  final val serviceName = "userService"
  final val currentVersion = "v1"

  final val context = "context"
  final val user = "user"
  final val group = "group"
  final val allowedUsers = "allowedUsers"

  val pathPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val pathContext = s"$pathPrefix/$context"
  def pathContextWithId(id: UUID) = s"$pathContext/$id"

  val pathUser = s"$pathPrefix/$user"
  def pathUserFind(providerId: String, externalUserId: String) = s"$pathUser/$providerId/$externalUserId"
  def pathUserDelete(id: UUID) = s"$pathUser/$id"

  val pathGroup = s"$pathPrefix/$group"
  def pathGroupWithId(id: UUID) = s"$pathGroup/$id"
  val pathGroupAllowedUsers = s"$pathGroup/$allowedUsers"

}
