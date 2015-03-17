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

case class CrazyFredEvent(header: EventHeader, message: String) extends Event

class CrazyFred(implicit override val almhirtContext: AlmhirtContext) extends CrazySheepActor {
  implicit val executionContext = almhirtContext.futuresContext
  private case object DoSomething

  private var cancel: Cancellable = null

  val rnd = new scala.util.Random()

  var määäh = 0

  def getSeverity(): Severity = {
    val n = rnd.nextInt(100)
    if (n < 10)
      MajorSeverity
    else
      MinorSeverity
  }

  def getProblem(msg: String): Problem = {
    määäh += 1
    val n = rnd.nextInt(100)
    val nextError = if (rnd.nextBoolean) None else Some(getError(msg + "x"))
    if (n < 10)
      NotFoundProblem(msg, cause = nextError)
    else if (n < 30)
      CollisionProblem(msg, cause = nextError)
    else
      ServiceBrokenProblem(msg, cause = nextError)
  }

  def getError(msg: String): ProblemCause = {
    if (rnd.nextBoolean)
      CauseIsProblem(getProblem(msg))
    else
      CauseIsThrowable(HasAThrowable(new Exception(msg)))
  }

  def receive: Receive = {
    case DoSomething ⇒
      if (rnd.nextBoolean) {
        reportMissedEvent(
          CrazyFredEvent(EventHeader(), s"määähähähhhääää-$määäh"),
          getSeverity,
          getProblem(s"määähähähhhääää-$määäh"))
      } else {
        reportFailure(getError(s"määähähähhhääää-$määäh"), getSeverity)
      }
  }

  override def preStart() {
    cancel = context.system.scheduler.schedule(3.seconds, 250.millis, self, DoSomething)
  }

  override def postStop() {
    cancel.cancel
  }
}