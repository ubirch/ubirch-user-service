package com.ubirch.user.server.formats

import com.ubirch.user.model._
import org.json4s.{CustomSerializer, JString, MappingException}

object UserFormats {

  def createStringFormat[A: Manifest](
                                       decode: String => A,
                                       validation: String => Boolean)(encode: A => String): CustomSerializer[A] = {
    val Class = implicitly[Manifest[A]].runtimeClass
    new CustomSerializer[A](_ =>
      ( {
        case JString(value) if validation(value) => decode(value)
        case JString(_) => throw new MappingException("Can't convert value to " + Class)
      }, {
        case a: A => JString(encode(a))
      }
      ))
  }

  private val actionRestTypeFormat: CustomSerializer[rest.Action] =
    createStringFormat(rest.Action.unsafeFromString, _.nonEmpty)(rest.Action.toFormattedString)
  private val actionDbTypeFormat: CustomSerializer[db.Action] =
    createStringFormat(db.Action.unsafeFromString, _.nonEmpty)(db.Action.toFormattedString)

  val all = List(actionRestTypeFormat, actionDbTypeFormat)

}
