package com.ubirch.user.model.rest

/**
  * author: cvandrei
  * since: 2017-03-23
  */
case class UserContext(context: String,
                       providerId: String,
                       externalUserId: String, // this is always the external userId
                       userName: String,
                       locale: String,
                       email: Option[String] = None
                      )

/**
  * Minimized version of [[UserContext]] used to find a user in our system.
  */
case class SimpleUserContext(context: String,
                             providerId: String,
                             userId: String // this is always the external userId
                            )
