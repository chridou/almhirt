package almhirt.domain.impl

import scalaz._, Scalaz._
import almhirt._
import almhirt.domain._
import almhirt.eventsourcing.DomainEventLog
import almhirt.almakka.AlmAkkaContext

abstract class BasicAggregateRootRepository[AR <: AggregateRoot[AR,Event], Event <: DomainEvent](eventLog: DomainEventLog, arFactory: CanCreateAggragateRoot[AR, Event], almAkka: AlmAkkaContext) extends HasAggregateRoots[AR, Event] with StoresAggregateRoots[AR, Event] {
  implicit private def timeout = almAkka.mediumDuration 
  implicit private def futureContext = almAkka.futureDispatcher 
  
  def get(id: java.util.UUID): AlmFuture[AR] =
    eventLog.getEvents(id)
      .map(e => e.map(_.asInstanceOf[Event]).toList)
      .mapV(events =>  
        if(events.isEmpty) NotFoundProblem("No aggregate root found with id '%s'".format(id)).failure
        else arFactory.rebuildFromHistory(NonEmptyList(events.head, events.tail: _*)))
  def store(ar: AR, uncommitedEvents: List[Event]): AlmFuture[AR] = 
    if(uncommitedEvents.isEmpty) AlmPromise(UnspecifiedProblem("no events", category = ApplicationProblem, severity = Minor).failure)
    else eventLog.storeEvents(uncommitedEvents).map(committedEvents => ar)
    
}