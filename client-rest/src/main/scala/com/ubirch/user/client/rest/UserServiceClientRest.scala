package com.ubirch.user.client.rest

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.client.rest.config.UserClientRestConfig
import com.ubirch.user.model.rest.Group

import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-05-15
  */
object UserServiceClientRest extends StrictLogging {

  implicit protected val groupRead: Reads[Group] = Json.reads[Group]

  def groups(contextName: String,
             providerId: String,
             externalUserId: String)
            (implicit ws: WSClient): Future[Option[Set[Group]]] = {

    val url = UserClientRestConfig.groups(
      contextName = contextName,
      providerId = providerId,
      externalUserId = externalUserId
    )

    // TODO how about connection pooling? is it built in?
    ws.url(url).get() map { res =>

      if (200 == res.status) {
        res.json.asOpt[Set[Group]]
      } else {
        logger.error(s"call to user-service REST API failed: status=${res.status}, body=${res.body}")
        None
      }

    }

  }

}
