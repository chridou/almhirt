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
import almhirt.core.Almhirt

class OperationStateTrackerActorHull(private val operationStateTracker: ActorRef)(implicit theAlmhirt: Almhirt) extends OperationStateTracker with ActorBased {
  val actor = operationStateTracker
  implicit private def executionContext = theAlmhirt.executionContext
  implicit private def timeout = theAlmhirt.defaultDuration

  def updateState(opState: OperationState) {
    operationStateTracker ! opState
  }

  def queryStateFor(ticket: TrackingTicket, within: Option[FiniteDuration] = None): AlmFuture[Option[OperationState]] = {
    val dur = within.getOrElse(theAlmhirt.durations.extraLongDuration)
    (operationStateTracker.ask(GetStateQry(ticket))(dur)).mapTo[OperationStateRsp].map(_.state)
  }

  def getResultFor(ticket: TrackingTicket, within: Option[FiniteDuration] = None): AlmFuture[ResultOperationState] = {
    val dur = within.getOrElse(theAlmhirt.durations.extraLongDuration)
    val future = (operationStateTracker ? RegisterResultCallbackQry(ticket))(dur)
    future.mapToSuccessfulAlmFuture[OperationStateResultRsp].mapV(x => x.state)
  }
}

object OperationStateTrackerActorHull {
  def apply(actor: ActorRef)(implicit almhirt: Almhirt): OperationStateTrackerActorHull =
    new OperationStateTrackerActorHull(actor)
}
