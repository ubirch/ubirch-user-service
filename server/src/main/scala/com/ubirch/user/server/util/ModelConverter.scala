package com.ubirch.user.server.util

import com.ubirch.user.model._
import com.ubirch.util.json.JsonFormats
import org.json4s.native.Serialization.write
import org.json4s.native.Serialization.read

import org.json4s.Formats

/**
  * author: cvandrei
  * since: 2017-03-30
  */
object ModelConverter {

  private implicit def json4sFormats: Formats = JsonFormats.default

  def toRest(dbContext: db.Context): rest.Context = {

    // TODO unit tests
    val json = write(dbContext)
    read[rest.Context](json)

  }

  def toDb(restContext: rest.Context): db.Context = {

    // TODO unit tests
    val json = write(restContext)
    read[db.Context](json)

  }

}
