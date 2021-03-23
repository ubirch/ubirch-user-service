package com.ubirch.user.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import com.ubirch.user.config.{Config, ConfigKeys}
import com.ubirch.user.server.route.MainRoute
import com.ubirch.util.mongo.connection.MongoUtil

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * author: cvandrei
  * since: 2017-03-22
  */
object Boot extends App with StrictLogging {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(Config.timeout, TimeUnit.SECONDS)

  implicit val mongo: MongoUtil = new MongoUtil(ConfigKeys.MONGO_PREFIX)

  val bindingFuture = start()
  registerShutdownHooks()

  private def start(): Future[ServerBinding] = {

    val interface = Config.interface
    val port = Config.port

    logger.info(s"start http server on $interface:$port")
    Http().bindAndHandle((new MainRoute).myRoute, interface, port)
  }

  private def registerShutdownHooks(): Unit = {

    Runtime.getRuntime.addShutdownHook(new Thread() {

      override def run(): Unit = {

        mongo.close()

        bindingFuture
          .flatMap(_.unbind())
          .onComplete(_ => system.terminate())

      }

    })

  }

}
