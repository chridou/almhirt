package almhirt.util.impl

import scalaz.syntax.validation._
import akka.util.Duration
import akka.util.duration._
import akka.pattern._
import almhirt._
import almhirt.util._
import almhirt.syntax.almfuture._
import almhirt.syntax.almvalidation._
import almhirt.messaging.MessageStream
import almhirt.environment.AlmhirtContext
import almhirt.commanding.DomainCommand
import almhirt.almakka.AlmActorLogging

class OperationStateTrackerWithoutTimeout(almhirtContext: AlmhirtContext) extends OperationStateTracker {
  import akka.actor._

  implicit private val executionContext = almhirtContext.system.futureDispatcher
  implicit private val timeout = almhirtContext.system.mediumDuration

  private val theActor = almhirtContext.system.actorSystem.actorOf(Props(new TheActor), "operationStateTracker")

  private class TheActor extends Actor with AlmActorLogging {
    var collectedInProcess: collection.Set[String] = collection.mutable.Set.empty
    var collectedResults: collection.Map[String, ResultOperationState] = collection.mutable.HashMap.empty
    var resultCallbacks: collection.Map[String, List[ResultOperationState => Unit]] = collection.mutable.HashMap.empty

    def receive: Receive = {
      case OperationStateReceived(opState) =>
        opState match {
          case InProcess(ticket) =>
            if (collectedResults.contains(ticket)) {
              log.warning("InProcess state for ticket %s cannot be set because there is already a result!".format(ticket))
            } else {
              if (!collectedInProcess.contains(ticket)) {
                val state = InProcess(ticket)
                collectedInProcess + ticket
                almhirtContext.reportOperationState(state)
              } else {
                log.warning("InProcess state for ticket %s already received!".format(ticket))
              }
            }
          case resState: ResultOperationState =>
            if (!collectedInProcess.contains(resState.ticket))
              log.warning("ResultState received but no InProcess state for ticket %s".format(resState.ticket))
            else
              collectedInProcess - resState.ticket

            if (collectedResults.contains(resState.ticket)) {
              log.warning("ResultState state for ticket %s will not be set because there is already a result! No callbacks will be triggered because any previously registered callback has already been triggered.".format(resState.ticket))
            } else {
              collectedResults + (resState.ticket -> resState)
              if (resultCallbacks.contains(resState.ticket)) {
                resultCallbacks(resState.ticket).foreach(callback => callback(resState))
                resultCallbacks - resState.ticket
              }
            }
        }
      case RegisterResultCallback(ticket, callback) =>
        if (collectedResults.contains(ticket))
          callback(collectedResults(ticket).success)
        else {
          if (resultCallbacks.contains(ticket))
            resultCallbacks + (ticket -> (callback :: resultCallbacks(ticket)))
          else
            resultCallbacks + (ticket -> List(callback))
        }

      case GetState(ticket) =>
        if (collectedInProcess.contains(ticket))
          sender ! Some(InProcess(ticket)).success
        else if (collectedResults.contains(ticket))
          sender ! Some(collectedResults(ticket)).success
        else
          sender ! None.success
    }

    override def preStart() {}
    override def postRestart(reason: Throwable) {}
    override def postStop() {}
  }

  private case class OperationStateReceived(opState: OperationState)
  private case class RegisterResultCallback(ticket: String, callback: AlmValidation[ResultOperationState] => Unit)
  private case class GetState(ticket: String)

  private class ResponseActor extends Actor {
    private var receiver: ActorRef = null
    def receive: Receive = {
      case "getResponse" =>
        receiver = sender
      case s: ResultOperationState =>
        receiver ! s
        context.stop(self)
    }
  }

  def updateState(opState: OperationState) {

  }

  def queryStateFor(ticket: String)(implicit atMost: Duration): AlmFuture[Option[OperationState]] =
    (theActor.ask(GetState(ticket))(atMost)).toAlmFuture[Option[OperationState]]

  def notifyResult(ticket: String, callback: AlmValidation[ResultOperationState] => Unit)(implicit atMost: Duration) {
    theActor ! RegisterResultCallback(ticket, callback)
  }

  def getResultFor(ticket: String)(implicit atMost: Duration): AlmFuture[ResultOperationState] = {
    val actor = almhirtContext.system.actorSystem.actorOf(Props(new ResponseActor))
    val future = (actor.ask("getResponse")(atMost)).toAlmFuture[ResultOperationState]
    notifyResult(ticket, resOpState => actor ! resOpState)(atMost)
    future
  }

  def dispose() = { almhirtContext.system.actorSystem.stop(theActor) }
}
