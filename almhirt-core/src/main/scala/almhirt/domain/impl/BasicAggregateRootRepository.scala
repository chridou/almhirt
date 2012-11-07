package almhirt.domain.impl

import scalaz._, Scalaz._
import almhirt._
import almhirt.syntax.almfuture._
import almhirt.domain._
import almhirt.eventlog.DomainEventLog
import almhirt.environment.AlmhirtContext
import almhirt.util._

abstract class BasicAggregateRootRepository[AR <: AggregateRoot[AR, Event], Event <: DomainEvent](eventLog: DomainEventLog, arFactory: CanCreateAggragateRoot[AR, Event], almhirtContext: AlmhirtContext) extends AggregateRootRepository[AR, Event] with CanValidateAggregateRootsAgainstEvents[AR, Event] {
  implicit private def timeout = almhirtContext.system.mediumDuration
  implicit private def futureContext = almhirtContext.system.futureDispatcher

  def get(id: java.util.UUID): AlmFuture[AR] =
    eventLog.getEvents(id)
      .map(e => e.map(_.asInstanceOf[Event]).toList)
      .mapV(events =>
        if (events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))

  def storeAndRetrieveUpdated(ar: AR, uncommittedEvents: List[Event]): AlmFuture[AR] =
    eventLog.getRequiredNextEventVersion(ar.id).flatMap(nextRequiredEventVersion => 
      validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion).continueWithFuture { case (ar, events) => 
        eventLog.storeEvents(uncommittedEvents).map(committedEvents => ar) } )

  def store(ar: AR, uncommittedEvents: List[Event], ticket: Option[String]): Unit =
    eventLog.getRequiredNextEventVersion(ar.id).flatMap(nextRequiredEventVersion => 
    validateAggregateRootAgainstEvents(ar, uncommittedEvents, nextRequiredEventVersion).continueWithFuture { case (ar, events) => 
      eventLog.storeEvents(uncommittedEvents) } )
      .onComplete(
        fail => updateFailedOperationState(almhirtContext, fail, ticket),
        succ => ticket.foreach(t => almhirtContext.reportOperationState(Executed(t))))

  private def updateFailedOperationState(context: AlmhirtContext, p: Problem, ticket: Option[String]) {
    context.reportProblem(p)
    ticket match {
      case Some(t) => context.reportOperationState(NotExecuted(t, p))
      case None => ()
    }
  }
}