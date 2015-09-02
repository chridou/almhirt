package almhirt.akkax

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._

sealed trait ComponentState

object ComponentState {
  case object Startup extends ComponentState
  case object Running extends ComponentState
  case object Paused extends ComponentState
  final case class Error(cause: almhirt.problem.ProblemCause) extends ComponentState
  case object PreparingForShutdown extends ComponentState
  case object ReadyForShutdown extends ComponentState
}

trait ComponentControl {
  def supports(action: ActorMessages.ComponentControlAction): Boolean
  def changeState(action: ActorMessages.ComponentControlAction): Unit

  def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState]
}

object ComponentControl {
  def apply(actor: ActorRef, supportedStateChangeActions: Set[ActorMessages.ComponentControlAction], logMsg: Option[(=> String) ⇒ Unit]): ComponentControl = new ComponentControl {
    def supports(action: ActorMessages.ComponentControlAction): Boolean = supportedStateChangeActions(action)
    def changeState(action: ActorMessages.ComponentControlAction): Unit =
      if (supports(action))
        actor ! action
      else
        logMsg.foreach(_(s"Attempt to execute an unsupportet state change action: $action"))

    def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState] =
      (actor ? ActorMessages.ReportComponentState)(timeout).mapCastTo[ComponentState]
  }
}