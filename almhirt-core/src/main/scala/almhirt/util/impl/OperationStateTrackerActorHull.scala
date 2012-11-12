package almhirt.util.impl

import scalaz.syntax.validation._
import akka.actor._
import akka.util.Duration
import akka.util.duration._
import akka.pattern._
import almhirt._
import almhirt.environment._
import almhirt.almfuture.all._
import almhirt.util._

class OperationStateTrackerActorHull(val actor: ActorRef)(implicit almhirtContext: AlmhirtContext) extends OperationStateTracker {

  implicit private val executionContext = almhirtContext.system.futureDispatcher
  implicit private val timeout = almhirtContext.system.mediumDuration

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
    val actor = almhirtContext.system.actorSystem.actorOf(Props(new ResponseActor))
    val future = (actor.ask("getResponse")(atMost)).mapToAlmFuture[ResultOperationState]
    onResult(ticket, resOpState => actor ! ResOpCmd(resOpState))(atMost)
    future
  }

  def dispose() = { almhirtContext.system.actorSystem.stop(actor) }
}
