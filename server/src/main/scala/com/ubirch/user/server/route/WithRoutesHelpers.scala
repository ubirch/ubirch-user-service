package com.ubirch.user.server.route

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.directives.FutureDirectives
import akka.pattern.CircuitBreaker
import com.typesafe.scalalogging.slf4j.StrictLogging
import com.ubirch.util.http.response.ResponseUtil

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.util.Try


trait WithRoutesHelpers extends StrictLogging with FutureDirectives {

  ru: ResponseUtil =>

  def system: ActorSystem

  def circuitBreaker(scheduler: Scheduler,
                     maxFailures: Int = 3,
                     callTimeout: FiniteDuration = 3 seconds,
                     resetTimeout: FiniteDuration = 6 seconds): CircuitBreaker = {
    CircuitBreaker(
      scheduler = scheduler,
      maxFailures = maxFailures,
      callTimeout = callTimeout,
      resetTimeout = resetTimeout
    )

  }

  def defaultCircuitBreaker: CircuitBreaker = circuitBreaker(system.scheduler)

  class OnComplete[T >: Any](future: => Future[T])(implicit ec: ExecutionContext, classTag: ClassTag[T]) {

    lazy val defaultCircuit: CircuitBreaker = defaultCircuitBreaker

    def onCompleteWithNoCircuitBreaker = onComplete(future.recover {
      case e: Exception =>
        logger.error("OOPs, (OC-CB) something happened: ", e)
        Future.failed(e)
    })

    def onCompleteWithCircuitBreaker(circuitBreaker: CircuitBreaker) = {
      onCompleteWithBreaker(circuitBreaker)(future.recover {
        case e: Exception =>
          logger.error("OOPs, (OC) something happened: ", e)
          Future.failed(e)
      })
    }

    def fold(maybeCircuitBreaker: => Option[CircuitBreaker] = Some(defaultCircuit)): Directive1[Try[T]] = {
      maybeCircuitBreaker.fold(onCompleteWithNoCircuitBreaker)(onCompleteWithCircuitBreaker)
    }

  }

  object OnComplete {
    def apply[T >: Any](future: => Future[T])(implicit ec: ExecutionContext, classTag: ClassTag[T]): OnComplete[T] =
      new OnComplete(future)
  }


}
