package almhirt.domain.impl

import scalaz._, Scalaz._
import almhirt._
import almhirt.domain._
import almhirt.eventlog.DomainEventLog
import almhirt.environment.AlmhirtContext

class BasicAggregateRootRepository[AR <: AggregateRoot[AR,Event], Event <: DomainEvent](eventLog: DomainEventLog, arFactory: CanCreateAggragateRoot[AR, Event], almhirtContext: AlmhirtContext) extends AggregateRootRepository[AR, Event] {
  implicit private def timeout = almhirtContext.akkaContext.mediumDuration 
  implicit private def futureContext = almhirtContext.akkaContext.futureDispatcher 
  
  def get(id: java.util.UUID): AlmFuture[AR] =
    eventLog.getEvents(id)
      .map(e => e.map(_.asInstanceOf[Event]).toList)
      .mapV(events =>  
        if(events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(events))

  def storeAndRetrieveUpdated(ar: AR, uncommitedEvents: List[Event]): AlmFuture[AR] =
    if(uncommitedEvents.isEmpty) AlmPromise(UnspecifiedProblem("no events", category = ApplicationProblem, severity = Minor).failure)
    else eventLog.storeEvents(uncommitedEvents).map(committedEvents => ar)

  def store(ar: AR, uncommitedEvents: List[Event], ticket: Option[String]): Unit = 
    if(uncommitedEvents.isEmpty) {
      val prob = UnspecifiedProblem("no events", category = ApplicationProblem, severity = Minor)
      updateFailedOperationState(almhirtContext, prob, ticket)
    }
    else {
      eventLog.storeEvents(uncommitedEvents)
        .onComplete(
          fail => updateFailedOperationState(almhirtContext, fail, ticket), 
          succ => ticket.foreach(t => almhirtContext.reportOperationState(Executed(t))))
    }
    
    
  private def updateFailedOperationState(context: AlmhirtContext, p: Problem, ticket: Option[String]) {
    context.reportProblem(p)
    ticket match {
      case Some(t) => context.reportOperationState(NotExecuted(t, p))
      case None => ()
    }
  }
}