package com.ubirch.user.server.route

import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.mongo.connection.MongoUtil

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  * author: cvandrei
  * since: 2017-03-22
  */
class MainRoute(implicit mongo: MongoUtil) {

  val welcome = new WelcomeRoute {}
  val context = new ContextRoute {}
  val user = new UserRoute {}
  val group = new GroupRoute {}
  val initData = new InitDataRoute {}

  val myRoute: Route = {

    pathPrefix(RouteConstants.apiPrefix) {
      pathPrefix(RouteConstants.serviceName) {
        pathPrefix(RouteConstants.currentVersion) {

          pathEndOrSingleSlash {
            welcome.route
          } ~
            context.route ~
            user.route ~
            group.route ~
            initData.route

        }
      }
    } ~ pathSingleSlash {
      welcome.route
    }

  }

}
