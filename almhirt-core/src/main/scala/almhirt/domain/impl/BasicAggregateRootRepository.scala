package almhirt.domain.impl

import scalaz._, Scalaz._
import almhirt._
import almhirt.domain._
import almhirt.eventsourcing.DomainEventLog

abstract class BasicAggregateRootRepository[AR <: AggregateRoot[AR,Event], Event <: DomainEvent](eventLog: DomainEventLog, arFactory: CanCreateAggragateRoot[AR, Event]) extends HasAggregateRoots[AR, Event] with StoresAggregateRoots[AR, Event] {
  def get(id: java.util.UUID): AlmFuture[AR] =
    eventLog.getEvents(id)
      .map(e => e.map(_.asInstanceOf[Event]).toList)
      .mapV(events =>  
        if(events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(NonEmptyList(events.head, events.tail: _*)))
  def store(ar: AR, uncommitedEvents: List[Event]): AlmFuture[AR] = 
    if(uncommitedEvents.isEmpty) AlmPromise(UnspecifiedProblem("no events", category = ApplicationProblem, severity = Minor).failure)
    else eventLog.storeEvents(uncommitedEvents).map(committedEvents => ar)
  def store(uncommitedEvents: List[Event]): AlmFuture[AR] =
    
}