package almhirt.util.impl

import scala.concurrent.duration.Duration
import scala.concurrent.duration.Duration._
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.core._
import almhirt.util._
import almhirt.syntax.almfuture._
import almhirt.syntax.almvalidation._
import almhirt.messaging.MessageStream
import almhirt.environment._
import almhirt.commanding.DomainCommand
import almhirt.almakka.AlmActorLogging
import almhirt.environment.configuration.ConfigHelper
import almhirt.environment.configuration.SystemHelper
import almhirt.core.Almhirt

class OperationStateTrackerWithoutTimeoutActor(implicit almhirt: Almhirt) extends Actor with AlmActorLogging with LogsProblemsTagged {
  val logTag = "OperationStateTracker"
  val collectedInProcess = collection.mutable.HashMap.empty[TrackingTicket, InProcess]
  val collectedResults = collection.mutable.HashMap.empty[TrackingTicket, ResultOperationState]
  val resultCallbacks = collection.mutable.HashMap.empty[TrackingTicket, List[ActorRef]]

  def receive: Receive = {
    case opState: OperationState =>
      opState match {
        case inProcess @ InProcess(ticket, _, _) =>
          if (collectedResults.contains(ticket)) {
            log.warning("InProcess state for ticket %s cannot be set because there is already a result!".format(ticket))
          } else {
            if (!collectedInProcess.contains(ticket)) {
              collectedInProcess += (ticket -> inProcess.withHeadCommandInfo)
            } else {
              log.warning("InProcess state for ticket %s already received!".format(ticket))
            }
          }
        case resState: ResultOperationState =>
          if (!collectedInProcess.contains(resState.ticket))
            log.warning("ResultState received but no InProcess state for ticket %s".format(resState.ticket))
          else
            collectedInProcess -= resState.ticket

          if (collectedResults.contains(resState.ticket)) {
            log.warning("ResultState state for ticket %s will not be changed because there is already a result! No callbacks will be triggered because any previously registered callback has already been triggered.".format(resState.ticket))
          } else {
            collectedResults += (resState.ticket -> resState)
            if (resultCallbacks.contains(resState.ticket)) {
              resultCallbacks(resState.ticket).foreach(_ ! OperationStateResultRsp(resState.ticket, resState.success))
              resultCallbacks - resState.ticket
            }
          }
      }
    case RegisterResultCallbackQry(ticket) =>
      if (collectedResults.contains(ticket)) {
        sender ! OperationStateResultRsp(ticket, collectedResults(ticket).success)
      } else {
        if (resultCallbacks.contains(ticket))
          resultCallbacks += (ticket -> (sender :: resultCallbacks(ticket)))
        else
          resultCallbacks += (ticket -> List(sender))
      }

    case GetStateQry(ticket) =>
      if (collectedInProcess.contains(ticket))
        sender ! OperationStateRsp(ticket, Some(collectedInProcess(ticket)).success)
      else if (collectedResults.contains(ticket))
        sender ! OperationStateRsp(ticket, Some(collectedResults(ticket)).success)
      else
        sender ! OperationStateRsp(ticket, None.success)
  }
}

class OperationStateTrackerWithoutTimeoutFactory extends OperationStateTrackerFactory {
  override def createActorRefComponent(args: Map[String, Any]): AlmValidation[ActorRef] =
    (args.lift >! "almhirt").flatMap(_.castTo[Almhirt].flatMap(theAlmhirt =>
      theAlmhirt.getConfig.flatMap(config =>
        ConfigHelper.operationState.getConfig(config).map { subConfig =>
          val name = ConfigHelper.operationState.getActorName(subConfig)
          val dispatcherName =
            ConfigHelper.getDispatcherNameFromComponentConfig(subConfig).fold(
              fail => {
                theAlmhirt.log.warning("No dispatchername found for OperationStateTracker. Using default Dispatcher")
                None
              },
              succ => {
                theAlmhirt.log.info(s"OperationStateTracker is using dispatcher '$succ'")
                Some(succ)
              })
          val props = SystemHelper.addDispatcherByNameToProps(dispatcherName)(Props(new OperationStateTrackerWithoutTimeoutActor()(theAlmhirt)))
          theAlmhirt.log.info(s"OperationStateTracker is OperationStateTrackerWithoutTimeout. Name is '$name'")
          theAlmhirt.actorSystem.actorOf(props, name)
        })))
}
