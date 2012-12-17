package almhirt.util.impl

import scalaz.syntax.validation._
import akka.actor._
import akka.util.Duration
import akka.util.duration._
import akka.pattern._
import almhirt.common._
import almhirt.core._
import almhirt.environment._
import almhirt.almfuture.all._
import almhirt.util._
import almhirt.common.AlmFuture

class OperationStateTrackerActorHull(val actor: ActorRef)(implicit baseOps: AlmhirtBaseOps, system: AlmhirtSystem) extends OperationStateTracker {

  implicit private def executionContext = baseOps.futureDispatcher
  implicit private def timeout = baseOps.mediumDuration

  private class ResponseActor extends Actor {
    private var receiver: ActorRef = null
    def receive: Receive = {
      case "getResponse" =>
        receiver = sender
      case ResOpCmd(resOp) =>
        receiver ! resOp
        context.stop(self)
    }
  }
  private case class ResOpCmd(res: AlmValidation[ResultOperationState])

  def updateState(opState: OperationState) {
    actor ! opState
  }

  def queryStateFor(ticket: TrackingTicket)(implicit atMost: Duration): AlmFuture[Option[OperationState]] =
    (actor.ask(GetStateQry(ticket))(atMost)).mapTo[OperationStateRsp].map(_.state)

  def onResult(ticket: TrackingTicket, callback: AlmValidation[ResultOperationState] => Unit)(implicit atMost: Duration) {
    actor ! RegisterResultCallbackCmd(ticket, callback, atMost)
  }

  def getResultFor(ticket: TrackingTicket)(implicit atMost: Duration): AlmFuture[ResultOperationState] = {
    val actor = system.actorSystem.actorOf(Props(new ResponseActor))
    val future = (actor.ask("getResponse")(atMost)).mapToAlmFuture[ResultOperationState]
    onResult(ticket, resOpState => actor ! ResOpCmd(resOpState))(atMost)
    future
  }

  def dispose() = { system.actorSystem.stop(actor) }
}
