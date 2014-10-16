package com.example.sillydemo.freakydogs

import scala.concurrent.duration._
import almhirt.common._
import almhirt.context.AlmhirtContext
import akka.actor.Cancellable
import almhirt.problem.Severity
import almhirt.problem.CauseIsProblem
import almhirt.problem.CauseIsThrowable
import almhirt.problem.ProblemCause
import almhirt.problem.HasAThrowable
import almhirt.akkax.AlmCircuitBreaker
import almhirt.akkax.CircuitControlSettings

class FreakySpike(implicit override val almhirtContext: AlmhirtContext) extends FreakyDogsActor {
  implicit val executionContext = almhirtContext.futuresContext

  private case object DoSomething
  private case object ChangeCircuitBreakerState

  val rnd = new scala.util.Random()

  val settings = CircuitControlSettings(
    maxFailures = 7,
    failuresWarnThreshold = None,
    callTimeout = 100.millis,
    resetTimeout = Some(10.seconds))

  val circuitBreaker = AlmCircuitBreaker(settings, almhirtContext.futuresContext, context.system.scheduler)

  var cancel: Cancellable = null

  def getSeverity(): Severity = {
    val n = rnd.nextInt(100)
    if (n == 0)
      CriticalSeverity
    else if (n < 30)
      MajorSeverity
    else
      MinorSeverity
  }

  var count = 0
  var mayBeGoodLeft = 100
  var badSent = 0

  def receive: Receive = {
    case DoSomething =>
      val res = if (mayBeGoodLeft > 0) {
        mayBeGoodLeft -= 1
        badSent = 0
        if (rnd.nextInt(100) < 90) {
          AlmFuture.delayedSuccess(80.millis)("Yeah!")
        } else {
          AlmFuture.delayedSuccess((rnd.nextInt(80) + 70).millis)("Yeah!")
        }
      } else {
        badSent += 1
        if (badSent == 10)
          mayBeGoodLeft = 100
        AlmFuture.failed(UnspecifiedProblem(s"Woof!"))
      }
      circuitBreaker.fused(res).onFailure({ problem =>
        reportFailure(problem, getSeverity)
      })
      context.system.scheduler.scheduleOnce((rnd.nextInt(300) + 100).millis, self, DoSomething)

    case ChangeCircuitBreakerState =>
      val r = rnd.nextInt(100) 
      if(r <= 10) {
        circuitBreaker.removeFuse()
      } else {
        circuitBreaker.attemptClose()
      }
  }

  override def preStart() {
    registerCircuitControl(circuitBreaker)
    self ! DoSomething
    self ! DoSomething
    self ! DoSomething
    self ! DoSomething
    self ! DoSomething
    cancel = context.system.scheduler.schedule(rnd.nextInt(4).seconds, (rnd.nextInt(10)+5).seconds, self, ChangeCircuitBreakerState)
  }

  override def postStop() {
    cancel.cancel()
    deregisterCircuitControl()
  }
}
