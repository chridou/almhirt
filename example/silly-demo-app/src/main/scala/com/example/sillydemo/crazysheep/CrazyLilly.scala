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

case class CrazyLillyEvent(header: EventHeader, message: String) extends Event

class CrazyLilly(implicit override val almhirtContext: AlmhirtContext) extends CrazySheepActor {
  implicit val executionContext = almhirtContext.futuresContext
  private case object DoSomething

  val rnd = new scala.util.Random()

  var määäh = 0

  def getSeverity(): Severity = {
    val n = rnd.nextInt(100)
    if (n == 0)
      CriticalSeverity
    else if (n < 30)
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
    case DoSomething =>
      if (rnd.nextBoolean) {
        reportMissedEvent(
          CrazyLillyEvent(EventHeader(), s"määähöhöhääähähhhääää-$määäh"),
          getSeverity,
          getProblem(s"mähhhääää-$määäh"))
      } else {
        reportFailure(getError(s"määähähää-$määäh"), getSeverity)
      }
      context.system.scheduler.scheduleOnce((rnd.nextInt(3000) + 100).millis, self, DoSomething)
  }

  override def preStart() {
    context.system.scheduler.scheduleOnce((rnd.nextInt(10000) + 5000).millis, self, DoSomething)
  }
}
