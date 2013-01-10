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

class OperationStateTrackerActorHull(val actor: ActorRef)(implicit almhirt: Almhirt) extends OperationStateTracker with ActorBased {
  implicit private def executionContext = almhirt.executionContext
  implicit private def timeout = almhirt.mediumDuration

  private class ResponseActor(callback: AlmValidation[ResultOperationState] => Unit) extends Actor {
    private var receiver: ActorRef = null
    def receive: Receive = {
      case OperationStateResultRsp(ticket, state) =>
        callback(state)
        if(receiver != null) receiver ! state
        context.stop(self)
      case "getResponse" =>
        receiver = sender
    }
  }
  private case class ResOpCmd(res: AlmValidation[ResultOperationState])

  def updateState(opState: OperationState) {
    actor ! opState
  }

  def queryStateFor(ticket: TrackingTicket)(implicit atMost: FiniteDuration): AlmFuture[Option[OperationState]] =
    (actor.ask(GetStateQry(ticket))(atMost)).mapTo[OperationStateRsp].map(_.state)

  def onResult(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit)(implicit atMost: FiniteDuration) {
    val replyTo = almhirt.system.actorSystem.actorOf(Props(new ResponseActor(callback)))
    actor ! RegisterResultCallbackQry(ticket, replyTo, atMost)
  }

  def getResultFor(ticket: TrackingTicket)(implicit atMost: FiniteDuration): AlmFuture[ResultOperationState] = {
    val replyTo = almhirt.system.actorSystem.actorOf(Props(new ResponseActor(_ => ())))
    val future = (replyTo ? "getResponse")(atMost).mapToAlmFuture[ResultOperationState]
    actor ! RegisterResultCallbackQry(ticket, replyTo, atMost)
    future
  }
}

object OperationStateTrackerActorHull {
  def apply(actor: ActorRef)(implicit almhirt: Almhirt): OperationStateTrackerActorHull =
    new OperationStateTrackerActorHull(actor)
}
