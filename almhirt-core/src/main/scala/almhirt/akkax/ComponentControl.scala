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
}

trait ComponentControl {
  def supportsPauseResume: Boolean
  def supportsRestart: Boolean
  def pause(): Unit
  def resume(): Unit
  def restart(): Unit
  
  def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState]
}

object ComponentControl {
  def apply(actor: ActorRef): ComponentControl = 
    new ComponentControl {
      def supportsPauseResume: Boolean = true
      def supportsRestart: Boolean = true
      def pause(): Unit = actor ! ActorMessages.Pause
      def resume(): Unit = actor! ActorMessages.Resume
      def restart(): Unit = actor! ActorMessages.Restart
    
      def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState] = (actor ? ActorMessages.ReportComponentState)(timeout).mapCastTo[ComponentState]
  }
  
  def reportsOnly(actor: ActorRef): ComponentControl = 
    new ComponentControl {
      def supportsPauseResume: Boolean = false
      def supportsRestart: Boolean = false
      def pause(): Unit = {}
      def resume(): Unit = {}
      def restart(): Unit = {}
    
      def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState] = (actor ? ActorMessages.ReportComponentState)(timeout).mapCastTo[ComponentState]
  }
  
  
  def pauseResume(actor: ActorRef): ComponentControl = 
    new ComponentControl {
      def supportsPauseResume: Boolean = true
      def supportsRestart: Boolean = false
      def pause(): Unit = actor ! ActorMessages.Pause
      def resume(): Unit = actor! ActorMessages.Resume
      def restart(): Unit = {}
    
      def state(timeout: FiniteDuration)(implicit executor: ExecutionContext): AlmFuture[ComponentState] = (actor ? ActorMessages.ReportComponentState)(timeout).mapCastTo[ComponentState]
  }
  
  val noActionTerminator: akka.actor.Actor.Receive = {
    case ActorMessages.Pause => 
    case ActorMessages.Resume => 
    case ActorMessages.Restart => 
  }
}