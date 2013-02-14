package almhirt.domain.impl

import scala.reflect.ClassTag
import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import almhirt.common._
import almhirt.core._
import almhirt.syntax.almfuture._
import almhirt.almvalidation.kit._
import almhirt.domain._
import almhirt.eventlog._
import almhirt.util._

abstract class UnsafeAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: ActorRef, arFactory: CanCreateAggragateRoot[AR, Event], theAlmhirt: Almhirt)(implicit tag: ClassTag[Event]) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = theAlmhirt.durations.longDuration
  implicit private val hasExecutionContext = theAlmhirt

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] = {
    val future = (eventLog ? GetEventsQry(id, None, None))(timeout).~+>[EventsForAggregateRootRsp]
    future
      .mapV { case EventsForAggregateRootRsp(id, DomainEventsChunk(idx, isLast, events), corrId) => events }
      .map(events => events.map(x => x.asInstanceOf[Event]))
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))
  }

  private def storeToEventLog(ar: AR, uncommittedEvents: IndexedSeq[Event], ticket: Option[TrackingTicket]) {
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
    } yield committedEvents).onComplete(
      fail =>
        updateFailedOperationState(theAlmhirt, fail, ticket),
      succ => {
        val action: PerformedAction =
          if (succ.isEmpty) PerformedUnspecifiedAction
          else if (succ.head.isInstanceOf[CreatingNewAggregateRootEvent]) PerformedCreateAction(AggregateRootRef(ar.id, succ.last.aggVersion + 1))
          else PerformedUpdateAction(AggregateRootRef(ar.id, succ.last.aggVersion + 1))
        ticket.foreach(t => theAlmhirt.publishOperationState(Executed(t, action)))
        succ.foreach(event => theAlmhirt.publishDomainEvent(event))
      })
  }

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
      storeToEventLog(ar.asInstanceOf[AR], uncommittedEvents.asInstanceOf[IndexedSeq[Event]], ticket)

  }
}