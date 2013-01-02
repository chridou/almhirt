package almhirt.domain.impl

import scalaz._, Scalaz._
import akka.actor._
import almhirt.core._
import almhirt.common._
import almhirt.syntax.almfuture._
import almhirt.domain._
import almhirt.eventlog.DomainEventLog
import almhirt.util._
import almhirt.common.AlmFuture
import almhirt.environment.Almhirt

abstract class BlockingAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: DomainEventLog, arFactory: CanCreateAggragateRoot[AR, Event], almhirt: Almhirt) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = almhirt.mediumDuration
  implicit private def futureContext = almhirt.executionContext

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] =
    eventLog.getEvents(id)
      .map(e => e.map(_.asInstanceOf[Event]).toList)
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))

  private def storeToEventLog(ar: AR, uncommittedEvents: List[Event], ticket: Option[TrackingTicket]) =
    eventLog.getRequiredNextEventVersion(ar.id).flatMap(nextRequiredEventVersion =>
      validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion).continueWithFuture {
        case (ar, events) => eventLog.storeEvents(uncommittedEvents)
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