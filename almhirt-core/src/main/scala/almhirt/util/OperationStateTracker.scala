package almhirt.util

import scalaz.syntax.validation._
import akka.util.Duration
import akka.util.duration._
import akka.pattern._
import almhirt._
import almhirt.syntax.almfuture._
import almhirt.syntax.almvalidation._
import almhirt.messaging.MessageStream
import almhirt.environment.AlmhirtContext
import almhirt.commanding.DomainCommand
import almhirt.almakka.AlmActorLogging

trait OperationStateTrackerCmd
case class UpdateOperationStateCmd(opState: OperationState) extends OperationStateTrackerCmd
case class RegisterResultCallbackCmd(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit, atMost: Duration) extends OperationStateTrackerCmd
case class GetStateQry(ticket: TrackingTicket) extends OperationStateTrackerCmd

trait OperationStateTrackerRsp
case class OperationStateRsp(ticket: TrackingTicket, state: AlmValidation[Option[OperationState]])

trait OperationStateTracker extends Disposable with ActorBased {
  def updateState(opState: OperationState): Unit
  def queryStateFor(ticket: TrackingTicket)(implicit atMost: Duration): AlmFuture[Option[OperationState]]
  def onResult(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit)(implicit atMost: Duration): Unit
  def getResultFor(ticket: TrackingTicket)(implicit atMost: Duration): AlmFuture[ResultOperationState]
}

object OperationStateTracker {
  import akka.actor._
  def apply()(implicit context: AlmhirtContext): AlmValidation[OperationStateTracker] = {
    val actor = context.system.actorSystem.actorOf(Props(new impl.OperationStateTrackerWithoutTimeoutActor), "operationStateTracker")
    new impl.OperationStateTrackerActorHull(actor).success
  }
}