package almhirt.util

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration._
import scalaz.syntax.validation._
import akka.actor.ActorRef
import akka.pattern._
import almhirt.common._
import almhirt.core._
import almhirt.syntax.almfuture._
import almhirt.syntax.almvalidation._
import almhirt.messaging.MessageStream
import almhirt.environment._
import almhirt.commanding.DomainCommand
import almhirt.almakka.AlmActorLogging
import almhirt.almakka.ActorBased
import almhirt.common.AlmFuture

trait OperationStateTrackerCmd
case class RegisterResultCallbackQry(ticket: TrackingTicket, replyTo: ActorRef, atMost: FiniteDuration) extends OperationStateTrackerCmd
case class GetStateQry(ticket: TrackingTicket) extends OperationStateTrackerCmd

trait OperationStateTrackerRsp
case class OperationStateRsp(ticket: TrackingTicket, state: AlmValidation[Option[OperationState]])
case class OperationStateResultRsp(ticket: TrackingTicket, state: AlmValidation[ResultOperationState])

trait OperationStateTracker {
  def updateState(opState: OperationState): Unit
  def queryStateFor(ticket: TrackingTicket)(implicit atMost: FiniteDuration): AlmFuture[Option[OperationState]]
  def onResult(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit)(implicit atMost: FiniteDuration): Unit
  def getResultFor(ticket: TrackingTicket)(implicit atMost: FiniteDuration): AlmFuture[ResultOperationState]
}

object OperationStateTracker {
  import akka.actor._
  def apply()(implicit almhirt: Almhirt): AlmValidation[OperationStateTracker] = {
    val actor = almhirt.system.actorSystem.actorOf(Props(new impl.OperationStateTrackerWithoutTimeoutActor), "operationStateTracker")
    new impl.OperationStateTrackerActorHull(actor).success
  }
}