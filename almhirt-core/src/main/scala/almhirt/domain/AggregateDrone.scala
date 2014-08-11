package almhirt.domain

import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.context.AlmhirtContext
import almhirt.streaming.PostOffice
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import scala.util.Success


object AggregateDrone {
  sealed trait AggregateDroneMessage
  
  case object GetManagedAggregateRoot extends AggregateDroneMessage
  sealed trait GetManagedAggregateRootResponse extends AggregateDroneMessage
  final case class ManagedAggregateRoot(ar: AggregateRoot) extends GetManagedAggregateRootResponse
  final case class AggregateRootDoesNotExist(id: AggregateRootId) extends GetManagedAggregateRootResponse
  final case class GetManagedAggregateRootFailed(id: AggregateRootId, problem: Problem) extends GetManagedAggregateRootResponse
  
  final case class ExecuteCommand(command: AggregateCommand) extends AggregateDroneMessage
  sealed trait ExecuteCommandResponse extends AggregateDroneMessage
  final case class CommandAccepted(commandId: CommandId) extends ExecuteCommandResponse
  final case class CommandNotAccepted(commandId: CommandId, problem: Option[Problem]) extends ExecuteCommandResponse
  
  sealed trait CommandExecutionResult extends AggregateDroneMessage
  final case class CommandExecuted(commandId: CommandId) extends CommandExecutionResult
  final case class CommandNotExecuted(commandId: CommandId, problem: Problem) extends CommandExecutionResult
}

private[domain] object DroneInternalMessages {
}

trait AggregateDrone[T <: AggregateRoot, E <: AggregateEvent] { me: Actor with ActorLogging with BuildsAggregateRoot[T, E] => 
  import AggregateDrone._
  import DroneInternalMessages._
  import almhirt.eventlog.AggregateEventLog._

  def domainEventsPostOffice: PostOffice[DomainEvent]
  def ahContext: AlmhirtContext
  def id: AggregateRootId
  def aggregateEventLog: ActorRef
  def snapshotStorage: Option[ActorRef]

  private case class InternalArBuildResult(ar: AggregateRootState[T])
  private case class InternalBuildArFailed(error: Throwable)

  private def receiveUninitialized: Receive = {
    case GetManagedAggregateRoot =>
      snapshotStorage match {
        case None => 
          context.become(receiveRebuildFromScratch(None, true))
          aggregateEventLog ! GetAllAggregateEventsFor(id)
        case _ => ???
      }

    case ExecuteCommand(command) =>
      context.parent ! CommandAccepted(command.id)
      snapshotStorage match {
        case None => 
          context.become(receiveRebuildFromScratch(None, true))
          aggregateEventLog ! GetAllAggregateEventsFor(id)
        case _ => ???
      }
  }

  private def receiveRebuildFromScratch(delayedCommand: Option[AggregateCommand], queryPending: Boolean): Receive = {
    case GetManagedAggregateRoot =>
      context.become(receiveRebuildFromScratch(delayedCommand, true))

    case ExecuteCommand(command) if delayedCommand.isEmpty =>
      context.parent ! CommandAccepted(command.id)
      context.become(receiveRebuildFromScratch(Some(command), queryPending))
 
    case ExecuteCommand(command) if delayedCommand.isDefined =>
      context.parent ! CommandNotAccepted(command.id, None)
      context.become(receiveRebuildFromScratch(Some(command), queryPending))
      
    case FetchedAggregateEvents(eventsEnumerator) =>
      val iteratee: Iteratee[AggregateEvent, AggregateRootState[T]] = Iteratee.fold[AggregateEvent, AggregateRootState[T]](NeverExisted) { case (acc, event) => 
        applyLifecycleAgnostic(acc, event.asInstanceOf[E])
      }(ahContext.futuresContext)
      
      eventsEnumerator.run(iteratee).onComplete{
        case scala.util.Success(arState) => 
          self ! InternalArBuildResult(arState)
        case scala.util.Failure(error) =>
          self ! InternalBuildArFailed(error)
      }(ahContext.futuresContext)

    case InternalArBuildResult(arState) =>
      arState match {
        case NeverExisted =>
          context.become(receiveDoesNotExist)
          ???
        case d: Dead =>
          context.become(receiveDeleted)
          ???
        case Alive(ar) =>
          context.become(receiveDeleted)
          ???
          
      }
      
    case InternalBuildArFailed(error) =>
      val problem = UnspecifiedProblem("An error has occured rebuilding the aggregate root.", cause = Some(error))

      if(queryPending) context.parent ! GetManagedAggregateRootFailed(id, problem) 
        
      delayedCommand.foreach(cmd => context.parent ! CommandNotExecuted(cmd.id, problem))
      
      context.become(receiveError(problem))
      
    case GetAggregateEventsFailed(problem) =>
      if(queryPending) context.parent ! GetManagedAggregateRootFailed(id, problem) 
        
      delayedCommand.foreach(cmd => context.parent ! CommandNotExecuted(cmd.id, problem))
      
      context.become(receiveError(problem))
      
  }
  
  private def receiveRebuildFromSnapshot(delayedCommand: Option[AggregateCommand], queryPending: Boolean): Receive = {
    case _ => ???
  }
  
  
  
  private def receiveDoesNotExist: Receive = {
    case GetManagedAggregateRoot => 
      context.parent ! AggregateRootDoesNotExist(id) 
  }

  
  private def receiveDeleted: Receive = {
    case GetManagedAggregateRoot => 
      context.parent ! AggregateRootDoesNotExist(id) 
  }

  private def receiveError(problem: Problem): Receive = {
    case GetManagedAggregateRoot => 
      context.parent ! GetManagedAggregateRootFailed(id, problem) 
      
    case ExecuteCommand(command) =>
      context.parent ! CommandNotAccepted(command.id, Some(problem))
     
  }

 
  
  def receive: Receive = receiveUninitialized
}