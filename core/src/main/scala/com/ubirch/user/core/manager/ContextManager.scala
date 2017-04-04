package com.ubirch.user.core.manager

import java.util.UUID

import com.typesafe.scalalogging.slf4j.StrictLogging

import com.ubirch.user.model.db.Context

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * author: cvandrei
  * since: 2017-03-30
  */
object ContextManager extends StrictLogging {

  def create(context: Context): Future[Context] = {

    // TODO implement
    Future(context)

  }

  def update(context: Context): Future[Context] = {

    // TODO implement
    Future(context)

  }

  def get(id: UUID): Future[Context] = {

    // TODO implement
    Future(Context(id, "foo-display-name-get"))

  }

  def findByName(name: String): Future[Context] = {

    // TODO implement
    Future(Context(displayName = name))

  }

  def delete(id: UUID): Future[Context] = {

    // TODO implement
    Future(Context(id, "foo-display-name-delete"))

  }

}
