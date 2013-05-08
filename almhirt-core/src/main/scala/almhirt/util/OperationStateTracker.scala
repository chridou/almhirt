package almhirt.util

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration._
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
import almhirt.core.Almhirt

trait OperationStateTrackerCmd
case class RegisterResultCallbackQry(ticket: TrackingTicket) extends OperationStateTrackerCmd
case class GetStateQry(ticket: TrackingTicket) extends OperationStateTrackerCmd

trait OperationStateTrackerRsp
case class OperationStateRsp(ticket: TrackingTicket, state: AlmValidation[Option[OperationState]])
case class OperationStateResultRsp(ticket: TrackingTicket, state: AlmValidation[ResultOperationState])

trait OperationStateTracker extends ActorBased {
  def updateState(opState: OperationState): Unit
  def queryStateFor(within: FiniteDuration)(ticket: TrackingTicket): AlmFuture[Option[OperationState]]
  def getResultFor(within: FiniteDuration)(ticket: TrackingTicket): AlmFuture[ResultOperationState]
}

object OperationStateTracker {
  import akka.actor._
  def apply()(implicit theAlmHirt: Almhirt): OperationStateTracker = {
    val actor = theAlmHirt.actorSystem.actorOf(Props(new impl.OperationStateTrackerWithoutTimeoutActor), "OperationStateTracker")
    new impl.OperationStateTrackerActorHull(actor)
  }
}