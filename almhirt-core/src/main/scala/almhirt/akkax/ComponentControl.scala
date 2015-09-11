package almhirt.akkax

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._

sealed trait ComponentState

object ComponentState {
  case object Startup extends ComponentState { override def toString = "Startup" }
  case object WaitingForStartSignal extends ComponentState { override def toString = "WaitingForStartSignal" }
  case object Running extends ComponentState { override def toString = "Running" }
  case object PreparingForPause extends ComponentState { override def toString = "PreparingForPause" }
  case object Paused extends ComponentState { override def toString = "Paused" }
  final case class Error(cause: almhirt.problem.ProblemCause) extends ComponentState { override def toString = s"Error: ${cause.message}" }
  case object PreparingForShutdown extends ComponentState { override def toString = "PreparingForShutdown" }
  case object ReadyForShutdown extends ComponentState { override def toString = "ReadyForShutdown" }
}

trait ComponentControl {
  def supports(action: ActorMessages.ComponentControlAction): Boolean

  def changeState(action: ActorMessages.ComponentControlAction): Unit

  def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState]
}

trait LocalComponentControl extends ComponentControl {
  def supportedActions: Set[ActorMessages.ComponentControlAction]
  def blockedChangeStateActions: Set[ActorMessages.ComponentControlAction]
}

object ComponentControl {
  def apply(actor: ActorRef, supportedStateChangeActions: Set[ActorMessages.ComponentControlAction], logMsg: Option[(⇒ String) ⇒ Unit]): ComponentControl = new ComponentControl {
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

object LocalComponentControl {
  def apply(actor: ActorRef, theSupportedStateChangeActions: Set[ActorMessages.ComponentControlAction], logMsg: Option[(⇒ String) ⇒ Unit]): LocalComponentControl =
    LocalComponentControl(actor, theSupportedStateChangeActions, Set.empty, logMsg)

  def apply(actor: ActorRef, theSupportedStateChangeActions: Set[ActorMessages.ComponentControlAction], theBlockedChangeStateActions: Set[ActorMessages.ComponentControlAction], logMsg: Option[(⇒ String) ⇒ Unit]): LocalComponentControl = new LocalComponentControl {
    val supportedActions = theSupportedStateChangeActions
    val blockedChangeStateActions = theBlockedChangeStateActions
    def supports(action: ActorMessages.ComponentControlAction): Boolean = theSupportedStateChangeActions(action)
    def changeState(action: ActorMessages.ComponentControlAction): Unit =
      if (supports(action) && !blockedChangeStateActions.contains(action))
        actor ! action
      else
        logMsg.foreach(_(s"Attempt to execute an unsupportet or blocked state change action: $action"))

    def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState] =
      (actor ? ActorMessages.ReportComponentState)(timeout).mapCastTo[ComponentState]
  }

  implicit class LocalComponentControlOps(val self: LocalComponentControl) extends AnyVal {
    def toRestrictedComponentControl: ComponentControl = new ComponentControl {
      def supports(action: ActorMessages.ComponentControlAction): Boolean = self.supports(action) && !self.blockedChangeStateActions(action)
      def changeState(action: ActorMessages.ComponentControlAction): Unit = self.changeState(action)
      def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState] = self.state(timeout)
    }
  }
}