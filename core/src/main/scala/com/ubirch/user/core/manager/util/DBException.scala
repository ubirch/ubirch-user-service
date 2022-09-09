package com.ubirch.user.core.manager.util

import com.typesafe.scalalogging.Logger

case class DBException(msg: String) extends Exception

object DBException {

  def handleError(methodDescription: String, ex: Throwable)(implicit logger: Logger): Nothing = {
    logger.error(s"database error: $methodDescription failed.", ex)
    throw DBException(s"database error: $methodDescription failed")
  }

  def handleError(methodDescription: String)(implicit logger: Logger): Nothing = {
    logger.error(s"database error: $methodDescription failed.")
    throw DBException(s"database error: $methodDescription failed")
  }

}
