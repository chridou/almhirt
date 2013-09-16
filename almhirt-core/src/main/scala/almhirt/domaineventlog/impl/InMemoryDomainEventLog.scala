package almhirt.domaineventlog.impl

import akka.actor._
import almhirt.domain.DomainEvent
import almhirt.domaineventlog.DomainEventLog

trait InMemoryDomainEventLog extends DomainEventLog { actor: Actor =>
  import DomainEventLog._

  private var domainEventLog = Vector.empty[DomainEvent]

  final protected def receiveDomainEventLogMsg: Receive = {
    case CommitDomainEvents(events) =>
      domainEventLog = domainEventLog ++ events
      sender ! CommittedDomainEvents(events)
      events.foreach(publishCommittedEvent)
    case GetAllDomainEvents =>
      sender ! FetchedDomainEventsBatch(domainEventLog)
    case GetDomainEvent(eventId) =>
      sender ! QueriedDomainEvent(eventId, domainEventLog.find(_.id == eventId))
    case GetAllDomainEventsFor(aggId) =>
      sender ! FetchedDomainEventsBatch(domainEventLog.filter(_.aggId == aggId))
    case GetDomainEventsFrom(aggId, fromVersion) =>
      sender ! FetchedDomainEventsBatch(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion >= fromVersion))
    case GetDomainEventsTo(aggId, toVersion) =>
      sender ! FetchedDomainEventsBatch(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion <= toVersion))
    case GetDomainEventsUntil(aggId, untilVersion) =>
      sender ! FetchedDomainEventsBatch(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion < untilVersion))
    case GetDomainEventsFromTo(aggId, fromVersion, toVersion) =>
      sender ! FetchedDomainEventsBatch(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion >= fromVersion && event.aggVersion <= toVersion))
    case GetDomainEventsFromUntil(aggId, fromVersion, untilVersion) =>
      sender ! FetchedDomainEventsBatch(domainEventLog.filter(event => event.aggId == aggId && event.aggVersion >= fromVersion && event.aggVersion < untilVersion))
  }
}