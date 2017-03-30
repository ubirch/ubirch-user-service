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

  def contextToRest(dbContext: db.Context): rest.Context = {

    // TODO unit tests
    val json = write(dbContext)
    read[rest.Context](json)

  }

  def contextToDb(restContext: rest.Context): db.Context = {

    // TODO unit tests
    val json = write(restContext)
    read[db.Context](json)

  }

  def userToRest(dbUser: db.User): rest.User = {

    // TODO unit tests
    val json = write(dbUser)
    read[rest.User](json)

  }

  def userToDb(restUser: rest.User): db.User = {

    // TODO unit tests
    val json = write(restUser)
    read[db.User](json)

  }

  def groupToRest(dbGroup: db.Group): rest.Group = {

    // TODO unit tests
    val json = write(dbGroup)
    read[rest.Group](json)

  }

  def groupToDb(restGroup: rest.Group): db.Group = {

    // TODO unit tests
    val json = write(restGroup)
    read[db.Group](json)

  }

}
