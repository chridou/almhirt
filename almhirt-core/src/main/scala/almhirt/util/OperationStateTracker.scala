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

trait OperationStateTracker extends Disposable {
  def updateState(opState: OperationState): Unit
  def queryStateFor(ticket: TrackingTicket)(implicit atMost: Duration): AlmFuture[Option[OperationState]]
  def onResult(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit)(implicit atMost: Duration): Unit
  def getResultFor(ticket: TrackingTicket)(implicit atMost: Duration): AlmFuture[ResultOperationState]
}

object OperationStateTracker {
  def apply()(implicit context: AlmhirtContext): OperationStateTracker = {
    new impl.OperationStateTrackerWithoutTimeout(context)
  }
}