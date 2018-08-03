package com.ubirch.user.server.route

import com.ubirch.user.util.server.RouteConstants
import com.ubirch.util.mongo.connection.MongoUtil

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

/**
  * author: cvandrei
  * since: 2017-03-22
  */
class MainRoute(implicit mongo: MongoUtil, system: ActorSystem) {

  private val welcome = new WelcomeRoute {}
  private val deepCheck = new DeepCheckRoute {}
  private val context = new ContextRoute {}
  private val user = new UserRoute {}
  private val group = new GroupRoute {}
  private val initData = new InitDataRoute {}
  private val register = new RegisterRoute {}

  val myRoute: Route = {

    pathPrefix(RouteConstants.apiPrefix) {
      pathPrefix(RouteConstants.serviceName) {
        pathPrefix(RouteConstants.currentVersion) {

          pathEndOrSingleSlash {
            welcome.route
          } ~ path(RouteConstants.check) {
            welcome.route
          } ~
            deepCheck.route ~
            context.route ~
            user.route ~
            group.route ~
            initData.route ~
            register.route

        }
      }
    } ~ pathSingleSlash {
      welcome.route
    }

  }

}
