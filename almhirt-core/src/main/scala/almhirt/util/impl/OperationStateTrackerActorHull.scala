package almhirt.util.impl

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration._
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import almhirt.common._
import almhirt.core._
import almhirt.environment._
import almhirt.almfuture.all._
import almhirt.util._
import almhirt.common.AlmFuture
import almhirt.almakka.ActorBased

class OperationStateTrackerActorHull(private val operationStateTracker: ActorRef)(implicit theAlmhirt: Almhirt) extends OperationStateTracker with ActorBased {
  val actor = operationStateTracker
  implicit private def executionContext = theAlmhirt.executionContext
  implicit private def timeout = theAlmhirt.defaultDuration

  def updateState(opState: OperationState) {
    operationStateTracker ! opState
  }

  def queryStateFor(atMost: FiniteDuration)(ticket: TrackingTicket): AlmFuture[Option[OperationState]] =
    (operationStateTracker.ask(GetStateQry(ticket))(atMost)).mapTo[OperationStateRsp].map(_.state)

  def getResultFor(atMost: FiniteDuration)(ticket: TrackingTicket): AlmFuture[ResultOperationState] = {
    val future = (operationStateTracker ? RegisterResultCallbackQry(ticket, atMost))(atMost)
    future.mapToSuccessfulAlmFuture[OperationStateResultRsp].mapV(x => x.state)
  }
}

object OperationStateTrackerActorHull {
  def apply(actor: ActorRef)(implicit almhirt: Almhirt): OperationStateTrackerActorHull =
    new OperationStateTrackerActorHull(actor)
}
