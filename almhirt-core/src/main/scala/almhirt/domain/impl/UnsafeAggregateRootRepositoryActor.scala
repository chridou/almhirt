package almhirt.domain.impl

import scalaz._, Scalaz._
import akka.actor._
import almhirt.core._
import almhirt.common._
import almhirt.syntax.almfuture._
import almhirt.domain._
import almhirt.eventlog.DomainEventLog
import almhirt.environment.AlmhirtContext
import almhirt.util._
import almhirt.common.AlmFuture
import almhirt.environment.AlmhirtBaseOps

abstract class UnsafeAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: DomainEventLog, arFactory: CanCreateAggragateRoot[AR, Event], baseOps: AlmhirtBaseOps) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = baseOps.mediumDuration
  implicit private def futureContext = baseOps.executionContext

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] =
    eventLog.getEvents(id)
      .map(e => e.map(_.asInstanceOf[Event]).toList)
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))

  private def storeToEventLog(ar: AR, uncommittedEvents: List[Event], ticket: Option[TrackingTicket]): Unit =
    eventLog.getRequiredNextEventVersion(ar.id).flatMap(nextRequiredEventVersion =>
      validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion).continueWithFuture {
        case (ar, events) => eventLog.storeEvents(uncommittedEvents)
      })
      .onComplete(
        fail =>
          updateFailedOperationState(baseOps, fail, ticket),
        succ => {
          ticket.foreach(t => baseOps.reportOperationState(Executed(t)))
          succ.foreach(event => baseOps.broadcastDomainEvent(event))
        })

  private def updateFailedOperationState(baseOps: AlmhirtBaseOps, p: Problem, ticket: Option[TrackingTicket]) {
    baseOps.reportProblem(p)
    ticket match {
      case Some(t) => baseOps.reportOperationState(NotExecuted(t, p))
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