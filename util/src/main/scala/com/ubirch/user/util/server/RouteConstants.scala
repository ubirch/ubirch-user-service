package com.ubirch.user.util.server

import java.net.URLEncoder
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
  final val users = "users"
  final val byIds = "byIds"
  final val group = "group"
  final val allowedUsers = "allowedUsers"
  final val memberOf = "memberOf"
  final val initData = "initData"

  final val externalIdExists = "extIdExists"
  final val recreate = "recreate"
  final val info = "info"
  final val activation = "activation"

  final val register = "register"

  val pathPrefix = s"/$apiPrefix/$serviceName/$currentVersion"

  val pathCheck = s"$pathPrefix/$check"
  val pathDeepCheck = s"$pathPrefix/$deepCheck"

  val pathContext = s"$pathPrefix/$context"

  def pathContextWithId(id: UUID) = s"$pathContext/$id"

  def pathContextFindByName(name: String): String = {

    val encodedName = URLEncoder.encode(name, "UTF-8")

    s"$pathContext/$byName/$encodedName"

  }

  val pathUser = s"$pathPrefix/$user"
  val pathRecreate = s"$pathPrefix/$user/$recreate"

  def pathUserFind(providerId: String, externalUserId: String): String = {

    val providerEncoded = URLEncoder.encode(providerId, "UTF-8")
    val userEncoded = URLEncoder.encode(externalUserId, "UTF-8")

    s"$pathUser/$providerEncoded/$userEncoded"
  }

  def pathUserUpdate(providerId: String, externalUserId: String): String = {

    val providerEncoded = URLEncoder.encode(providerId, "UTF-8")
    val userEncoded = URLEncoder.encode(externalUserId, "UTF-8")

    s"$pathUser/$providerEncoded/$userEncoded"

  }

  def pathUserDelete(providerId: String, externalUserId: String): String = {

    val providerEncoded = URLEncoder.encode(providerId, "UTF-8")
    val userEncoded = URLEncoder.encode(externalUserId, "UTF-8")

    s"$pathUser/$providerEncoded/$userEncoded"

  }

  def pathExternalIdExists(externalId: String): String = {

    val userEncoded = URLEncoder.encode(externalId, "UTF-8")

    s"$pathUser/$externalIdExists/$userEncoded"

  }

  val pathGroup = s"$pathPrefix/$group"

  def pathGroupWithId(id: UUID) = s"$pathGroup/$id"

  val pathGroupAllowedUsers = s"$pathGroup/$allowedUsers"

  def pathGroupMemberOf(contextName: String,
                        providerId: String,
                        externalUserId: String
                       ): String = {

    val contextEncoded = URLEncoder.encode(contextName, "UTF-8")
    val providerEncoded = URLEncoder.encode(providerId, "UTF-8")
    val userEncoded = URLEncoder.encode(externalUserId, "UTF-8")

    s"$pathPrefix/$group/$memberOf/$contextEncoded/$providerEncoded/$userEncoded"

  }

  final def pathInitData(env: String) = s"$pathPrefix/$initData/$env"

  final val pathRegister = s"$pathPrefix/$register"

  private val pathUserInfo = s"$pathUser/$info"
  final def pathUserInfoGET(context: String, providerId: String, userId: String): String = {

    val contextEncoded = URLEncoder.encode(context, "UTF-8")
    val providerEncoded = URLEncoder.encode(providerId, "UTF-8")
    val userEncoded = URLEncoder.encode(userId, "UTF-8")

    s"$pathUserInfo/$contextEncoded/$providerEncoded/$userEncoded"

  }
  final val pathUserInfoPUT = pathUserInfo

}
