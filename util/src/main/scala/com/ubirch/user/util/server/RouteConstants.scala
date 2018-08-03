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

  final val check = "check"
  final val deepCheck = "deepCheck"

  final val context = "context"
  final val byName = "byName"
  final val user = "user"
  final val group = "group"
  final val allowedUsers = "allowedUsers"
  final val memberOf = "memberOf"
  final val initData = "initData"

  final val externalIdExists = "extIdExists"

  final val register = "register"
  final val userInfo = "userInfo"

  val pathPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val pathCheck = s"$pathPrefix/$check"
  val pathDeepCheck = s"$pathPrefix/$deepCheck"

  val pathContext = s"$pathPrefix/$context"

  def pathContextWithId(id: UUID) = s"$pathContext/$id"

  def pathContextFindByName(name: String) = s"$pathContext/$byName/$name"

  val pathUser = s"$pathPrefix/$user"

  def pathUserFind(providerId: String, externalUserId: String) = s"$pathUser/$providerId/$externalUserId"

  def pathUserUpdate(providerId: String, externalUserId: String) = s"$pathUser/$providerId/$externalUserId"

  def pathUserDelete(providerId: String, externalUserId: String) = s"$pathUser/$providerId/$externalUserId"

  def pathExternalIdExists(externalId: String) = s"$pathUser/$externalIdExists/$externalId"

  val pathGroup = s"$pathPrefix/$group"

  def pathGroupWithId(id: UUID) = s"$pathGroup/$id"

  val pathGroupAllowedUsers = s"$pathGroup/$allowedUsers"

  def pathGroupMemberOf(contextName: String,
                        providerId: String,
                        externalUserId: String
                       ): String = {
    s"$pathPrefix/$group/$memberOf/$contextName/$providerId/$externalUserId"
  }

  final def pathInitData(env: String) = s"$pathPrefix/$initData/$env"

  final val pathRegister = s"$pathPrefix/$register"
  final def pathUserInfoGET(context: String, providerId: String, userId: String) = s"$pathPrefix/$userInfo/$context/$providerId/$userId"
  final val pathUserInfoPUT = s"$pathPrefix/$userInfo"

}
