package com.ubirch.user.client.rest

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.client.rest.config.UserClientRestConfig
import com.ubirch.user.model.rest.Group
import com.ubirch.util.json.MyJsonProtocol

import org.json4s.native.Serialization.read

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-05-15
  */
object UserServiceClientRest extends MyJsonProtocol
  with StrictLogging {

  def groups(contextName: String,
             providerId: String,
             externalUserId: String)
            (implicit httpClient: HttpExt, materializer: Materializer): Future[Option[Set[Group]]] = {

    logger.debug("groups(): query groups through REST API")
    val url = UserClientRestConfig.groups(
      contextName = contextName,
      providerId = providerId,
      externalUserId = externalUserId
    )

    httpClient.singleRequest(HttpRequest(uri = url)) flatMap {

      case HttpResponse(StatusCodes.OK, _, entity, _) =>

        entity.dataBytes.runFold(ByteString(""))(_ ++ _) map { body =>
          Some(read[Set[Group]](body.utf8String))
        }

      case res@HttpResponse(code, _, _, _) =>

        res.discardEntityBytes()
        Future(
          logErrorAndReturnNone(s"groups() call to user-service REST API failed: url=$url, code=$code")
        )

    }

  }

  private def logErrorAndReturnNone[T](errorMsg: String,
                                       t: Option[Throwable] = None
                                      ): Option[T] = {
    t match {
      case None => logger.error(errorMsg)
      case Some(someThrowable: Throwable) => logger.error(errorMsg, someThrowable)
    }

    None

  }

}
