package almhirt.domain.impl

import scalaz._, Scalaz._
import akka.actor._
import akka.pattern._
import akka.util.Timeout._
import almhirt.core._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.domain._
import almhirt.eventlog._
import almhirt.util._
import almhirt.common.AlmFuture
import almhirt.environment.Almhirt

abstract class BlockingAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: ActorRef, arFactory: CanCreateAggragateRoot[AR, Event], almhirt: Almhirt) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = almhirt.mediumDuration
  implicit private def futureContext = almhirt.executionContext

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] =
    (eventLog ? GetEventsQry(id))(timeout)
      .mapTo[EventsForAggregateRootRsp]
      .map(x => x.chunk.events)
      .map(e => e.map(events => events.map(_.asInstanceOf[Event]).toList))
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))

  private def storeToEventLog(ar: AR, uncommittedEvents: List[Event], ticket: Option[TrackingTicket]) =
    (eventLog ? GetRequiredNextEventVersionQry(ar.id))(timeout)
      .mapTo[RequiredNextEventVersionRsp]
      .map(x => x.nextVersion)
      .toAlmFuture
      .flatMap(nextRequiredEventVersion =>
      validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion).continueWithFuture {
        case (ar, events) => 
          (eventLog ? LogEventsQry(events, None))(timeout)
          .mapTo[CommittedDomainEventsRsp]
          .map(_.events)
      })
      .andThen(
        fail =>
          updateFailedOperationState(almhirt, fail, ticket),
        succ => {
          ticket.foreach(t => almhirt.reportOperationState(Executed(t)))
          succ.foreach(event => almhirt.broadcastDomainEvent(event))
        }).awaitResult

  private def updateFailedOperationState(almhirt: Almhirt, p: Problem, ticket: Option[TrackingTicket]) {
    almhirt.reportProblem(p)
    ticket match {
      case Some(t) => almhirt.reportOperationState(NotExecuted(t, p))
      case None => ()
    }
  }

  def receive: Receive = {
    case GetAggregateRootQry(aggId) =>
      val res = getFromEventLog(aggId).awaitResult
      sender ! AggregateRootFromRepositoryRsp[AR, Event](res)
    case StoreAggregateRootCmd(ar, uncommittedEvents, ticket) =>
      storeToEventLog(ar.asInstanceOf[AR], uncommittedEvents.asInstanceOf[List[Event]], ticket)
  }
}