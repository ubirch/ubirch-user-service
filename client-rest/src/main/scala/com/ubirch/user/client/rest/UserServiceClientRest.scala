package com.ubirch.user.client.rest

import com.ubirch.user.client.rest.config.UserClientRestConfig
import com.ubirch.user.model.rest.Group

import play.api.libs.ws.WSClient

import scala.concurrent.Future


/**
  * author: cvandrei
  * since: 2017-05-15
  */
object UserServiceClientRest {

  def groups(contextName: String,
             providerId: String,
             externalUserId: String)
            (implicit ws: WSClient): Future[Set[Group]] = {


    val url = UserClientRestConfig.groups(
      contextName = contextName,
      providerId = providerId,
      externalUserId = externalUserId
    )

    ws.url(url).get() map { response =>
      // TODO process response
      Set.empty
    }

  }

}
