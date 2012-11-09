package almhirt.domain.impl

import scalaz._, Scalaz._
import akka.actor._
import almhirt._
import almhirt.syntax.almfuture._
import almhirt.domain._
import almhirt.eventlog.DomainEventLog
import almhirt.environment.AlmhirtContext
import almhirt.util._

abstract class UnsafeAggregateRootRepositoryActor[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: DomainEventLog, arFactory: CanCreateAggragateRoot[AR, Event], almhirtContext: AlmhirtContext) extends Actor {
  private val validator = new CanValidateAggregateRootsAgainstEvents[AR, Event] {}
  implicit private def timeout = almhirtContext.system.mediumDuration
  implicit private def futureContext = almhirtContext.system.futureDispatcher

  private def getFromEventLog(id: java.util.UUID): AlmFuture[AR] =
    eventLog.getEvents(id)
      .map(e => e.map(_.asInstanceOf[Event]).toList)
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))

  private def storeToEventLog(ar: AR, uncommittedEvents: List[Event], ticket: Option[String]): Unit =
    eventLog.getRequiredNextEventVersion(ar.id).flatMap(nextRequiredEventVersion =>
      validator.validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion).continueWithFuture {
        case (ar, events) => eventLog.storeEvents(uncommittedEvents)
      })
      .onComplete(
        fail =>
          updateFailedOperationState(almhirtContext, fail, ticket),
        succ => {
          ticket.foreach(t => almhirtContext.reportOperationState(Executed(t)))
          succ.foreach(event => almhirtContext.broadcastDomainEvent(event))
        })

  private def updateFailedOperationState(context: AlmhirtContext, p: Problem, ticket: Option[String]) {
    context.reportProblem(p)
    ticket match {
      case Some(t) => context.reportOperationState(NotExecuted(t, p))
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