package com.example.sillydemo.crazysheep

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

case class CrazyShawnEvent(header: EventHeader, message: String) extends Event
case class CrazyShawnCommand(header: CommandHeader, message: String) extends Command

class CrazyShawn(implicit override val almhirtContext: AlmhirtContext) extends CrazySheepActor {
  implicit val executionContext = almhirtContext.futuresContext

  private case class DoSomething(event: Event)
  private case object RejectCommand

  val rnd = new scala.util.Random()

  val settings = CircuitControlSettings(
    maxFailures = 7,
    failuresWarnThreshold = None,
    callTimeout = 100.millis,
    resetTimeout = Some(10.seconds))

  val circuitBreaker = AlmCircuitBreaker(settings, almhirtContext.futuresContext, context.system.scheduler)

  def getSeverity(): Severity = {
    val n = rnd.nextInt(100)
    if (n == 0)
      CriticalSeverity
    else if (n < 30)
      MajorSeverity
    else
      MinorSeverity
  }

  def getEvent(msg: String): Event = {
    val n = rnd.nextInt(100)
    if (n < 10)
      CrazyLillyEvent(EventHeader(), msg)
    else if (n < 30)
      CrazyFredEvent(EventHeader(), msg)
    else
      CrazyShawnEvent(EventHeader(), msg)
  }

  var count = 0
  var mayBeGoodLeft = 100
  var badSent = 0

  def receive: Receive = {
    case RejectCommand ⇒
      val cmd = CrazyShawnCommand(CommandHeader(), s"$count")
      reportRejectedCommand(cmd, getSeverity, UnspecifiedProblem("Huh?"))
    
    case DoSomething(event: Event) ⇒
      val res = if (mayBeGoodLeft > 0) {
        mayBeGoodLeft -= 1
        badSent = 0
        if (rnd.nextInt(100) < 90) {
          AlmFuture.delayedSuccess(80.millis)("Yeah!")
        } else {
         AlmFuture.delayedSuccess((rnd.nextInt(80)+70).millis)("Yeah!")
        }
      } else {
        badSent += 1
        if (badSent == 10)
          mayBeGoodLeft = 100
        AlmFuture.failed(UnspecifiedProblem(s"Huuh! Failed on: $event"))
      }
      circuitBreaker.fused(res).onFailure({ problem ⇒
        reportMissedEvent(event, getSeverity, problem)
        reportFailure(problem, getSeverity)
        self ! RejectCommand
      })
      count += 1
      val nextevent = getEvent(s"mähähä-$count")
      context.system.scheduler.scheduleOnce((rnd.nextInt(300) + 100).millis, self, DoSomething(nextevent))
  }

  override def preStart() {
    registerCircuitControl(circuitBreaker)
    self ! DoSomething(getEvent("first"))
    self ! DoSomething(getEvent("second"))
    self ! DoSomething(getEvent("third"))
    self ! DoSomething(getEvent("fourth"))
    self ! DoSomething(getEvent("fifth"))
  }

  override def postStop() {
    deregisterCircuitControl()
  }
}
