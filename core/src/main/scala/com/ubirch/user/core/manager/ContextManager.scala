package com.ubirch.user.core.manager

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.rest.Context
import com.ubirch.util.uuid.UUIDUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object ContextManager extends StrictLogging {

  def create(contextRest: Context): Future[Context] = {
    // TODO implement
    Future(contextRest.copy(id = Some(UUIDUtil.uuid)))
  }

  def update(contextRest: Context): Future[Context] = {
    // TODO implement
    Future(contextRest)
  }

  def get(id: UUID): Future[Context] = {
    // TODO implement
    Future(Context(Some(id), "foo-display-name-get"))
  }

  def delete(id: UUID): Future[Context] = {
    // TODO implement
    Future(Context(Some(id), "foo-display-name-delete"))
  }

}
