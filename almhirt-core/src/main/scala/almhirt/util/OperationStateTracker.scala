package almhirt.util

import scalaz.syntax.validation._
import akka.util.Duration
import akka.util.duration._
import akka.pattern._
import almhirt.common._
import almhirt.core._
import almhirt.syntax.almfuture._
import almhirt.syntax.almvalidation._
import almhirt.messaging.MessageStream
import almhirt.environment._
import almhirt.commanding.DomainCommand
import almhirt.almakka.AlmActorLogging
import almhirt.common.ActorBased
import almhirt.common.AlmFuture

trait OperationStateTrackerCmd
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
  def apply()(implicit baseOps: AlmhirtBaseOps, system: AlmhirtSystem): AlmValidation[OperationStateTracker] = {
    val actor = system.actorSystem.actorOf(Props(new impl.OperationStateTrackerWithoutTimeoutActor), "operationStateTracker")
    new impl.OperationStateTrackerActorHull(actor).success
  }
}