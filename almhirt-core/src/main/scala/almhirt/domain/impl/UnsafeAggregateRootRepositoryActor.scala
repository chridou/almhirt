package almhirt.domain.impl

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import almhirt.common._
import almhirt.core._
import almhirt.syntax.almfuture._
import almhirt.syntax.almvalidation._
import almhirt.domain._
import almhirt.eventlog._
import almhirt.util._

abstract class UnsafeAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: ActorRef, arFactory: CanCreateAggragateRoot[AR, Event], theAlmhirt: Almhirt)(implicit tag: ClassTag[Event]) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = theAlmhirt.durations.longDuration
  implicit private val hasExecutionContext = theAlmhirt

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] = {
    val future = (eventLog ? GetEventsQry(id, None, None))(timeout).mapTo[EventsForAggregateRootRsp]
    future
    	.mapOver{ case EventsForAggregateRootRsp(id, DomainEventsChunk(idx, isLast, events), corrId) => events }
    	.map(events => events.map(x => x.asInstanceOf[Event]))
    	.mapV(events =>
          if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
          else arFactory.rebuildFromHistory(events))
  }

  private def storeToEventLog(ar: AR, uncommittedEvents: List[Event], ticket: Option[TrackingTicket]): Unit =
    for {
      nextRequiredVersion <- 
      	(eventLog ? GetRequiredNextEventVersionQry(ar.id))(timeout).mapTo[CommittedDomainEventsRsp].mapOver(x => x.success)
      x <-??
    } yield nextRequiredVersion
//    validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion).continueWithFuture
//    (eventLog ? LogEventsQry)(timeout).mapTo[CommittedDomainEventsRsp].mapOver{ case CommittedDomainEventsRsp(events, corrId) =>
//      }
    ???
//    eventLog.getRequiredNextEventVersion(ar.id).flatMap(nextRequiredEventVersion =>
//      validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion).continueWithFuture {
//        case (ar, events) => eventLog.storeEvents(uncommittedEvents)
//      })
//      .onComplete(
//        fail =>
//          updateFailedOperationState(almhirt, fail, ticket),
//        succ => {
//          ticket.foreach(t => almhirt.publishOperationState(Executed(t)))
//          succ.foreach(event => almhirt.publishDomainEvent(event))
//        })

  private def updateFailedOperationState(almhirt: Almhirt, p: Problem, ticket: Option[TrackingTicket]) {
    almhirt.publishProblem(p)
    ticket match {
      case Some(t) => almhirt.publishOperationState(NotExecuted(t, p))
      case None => ()
    }
  }

  def receive: Receive = {
    case GetAggregateRootQry(aggId) =>
      val pinnedSender = sender
      getFromEventLog(aggId).onComplete(pinnedSender ! AggregateRootFromRepositoryRsp[AR, Event](_))
    case StoreAggregateRootCmd(ar, uncommittedEvents, ticket) =>
      storeToEventLog(ar.asInstanceOf[AR], uncommittedEvents.asInstanceOf[List[Event]], ticket)

  }
}