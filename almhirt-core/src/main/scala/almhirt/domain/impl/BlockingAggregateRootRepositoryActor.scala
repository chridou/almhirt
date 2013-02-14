package almhirt.domain.impl

import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import almhirt.core._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.domain._
import almhirt.eventlog._
import almhirt.util._
import almhirt.common.AlmFuture
import almhirt.core.Almhirt

abstract class BlockingAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: ActorRef, arFactory: CanCreateAggragateRoot[AR, Event], theAlmhirt: Almhirt) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = theAlmhirt.defaultDuration
  implicit private val hasExecutionContext = theAlmhirt
  implicit private val executionContext = hasExecutionContext.executionContext

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] = {
    val future = (eventLog ? GetEventsQry(id, None, None))(timeout).~+>[EventsForAggregateRootRsp]
    future
      .mapV { case EventsForAggregateRootRsp(id, DomainEventsChunk(idx, isLast, events), corrId) => events }
      .map(events => events.map(x => x.asInstanceOf[Event]))
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))
  }

  private def storeToEventLog(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: Option[TrackingTicket]) = {
    inTryCatchM {
      (for {
        response <- (eventLog ? GetRequiredNextEventVersionQry(ar.id))(timeout).~+>[RequiredNextEventVersionRsp]
        validated <- AlmFuture {
          for {
            nextRequiredEventVersion <- response.nextVersion
            validated <- validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion)
          } yield validated
        }
        committedEventsRsp <- (eventLog ? LogEventsQry(uncommittedEvents, None))(timeout).~+>[CommittedDomainEventsRsp]
        committedEvents <- AlmFuture { committedEventsRsp.events }
      } yield committedEvents).andThen(
        fail =>
          updateFailedOperationState(fail, ticket),
        succ => {
          val action: PerformedAction =
            if (succ.isEmpty) PerformedUnspecifiedAction
            else if (succ.head.isInstanceOf[CreatingNewAggregateRootEvent]) PerformedCreateAction(AggregateRootRef(ar.id, succ.last.aggVersion + 1))
            else PerformedUpdateAction(AggregateRootRef(ar.id, succ.last.aggVersion + 1))
          ticket.foreach(t => theAlmhirt.publishOperationState(Executed(t, action)))
          succ.foreach(event => theAlmhirt.publishDomainEvent(event))
        }).awaitResult
    }(s"Could not store ${uncommittedEvents.size} events for aggregate root ${ar.id}").onFailure(p => updateFailedOperationState(p, ticket))
  }

  private def updateFailedOperationState(p: Problem, ticket: Option[TrackingTicket]) {
    theAlmhirt.publishProblem(p)
    ticket match {
      case Some(t) => theAlmhirt.publishOperationState(NotExecuted(t, p))
      case None => ()
    }
  }

  def receive: Receive = {
    case GetAggregateRootQry(aggId) =>
      val res = getFromEventLog(aggId).awaitResult
      sender ! AggregateRootFromRepositoryRsp[AR, Event](res)
    case StoreAggregateRootCmd(ar, uncommittedEvents, ticket) =>
      storeToEventLog(ar.asInstanceOf[AR], uncommittedEvents.asInstanceOf[IndexedSeq[Event]], ticket)
  }
}