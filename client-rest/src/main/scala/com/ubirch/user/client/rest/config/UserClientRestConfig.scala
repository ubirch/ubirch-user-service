package com.ubirch.user.client.rest.config

import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.config.ConfigBase

/**
  * author: cvandrei
  * since: 2017-05-15
  */
object UserClientRestConfig extends ConfigBase {

  /**
    * The host the REST API runs on.
    *
    * @return host
    */
  private def host = config.getString(UserClientRestConfigKeys.HOST)

  val urlCheck = s"$host${RouteConstants.pathCheck}"

  val urlDeepCheck = s"$host${RouteConstants.pathDeepCheck}"

  def groups(contextName: String,
             providerId: String,
             externalUserId: String): String = {

    val path = RouteConstants.pathGroups(contextName = contextName,
      providerId = providerId,
      externalUserId = externalUserId
    )

    s"$host$path"

  }

}
