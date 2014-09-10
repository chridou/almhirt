package almhirt.eventlog

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.configuration._
import play.api.libs.iteratee.Enumerator

object InMemoryAggregateRootEventLog {
  def props(): Props = 
    Props(new InMemoryAggregateRootEventLog())
}

class InMemoryAggregateRootEventLog extends Actor with ActorLogging {
  import AggregateRootEventLog._

  private var domainEventLog = Vector.empty[AggregateRootEvent]

  def receive: Receive = {
    case CommitAggregateRootEvent(event) ⇒
      domainEventLog = domainEventLog :+ event
      sender() ! AggregateRootEventCommitted(event.eventId)
    case GetAllAggregateRootEvents ⇒
      sender() ! FetchedAggregateRootEvents(Enumerator(domainEventLog: _*))
    case GetAggregateRootEvent(eventId) ⇒
      sender() ! FetchedAggregateRootEvent(eventId, domainEventLog.find(_.eventId == eventId))
    case GetAllAggregateRootEventsFor(aggId) ⇒
      sender() ! FetchedAggregateRootEvents(Enumerator(domainEventLog.filter(_.aggId == aggId): _*))
    case GetAggregateRootEventsFrom(aggId, fromVersion) ⇒
      sender() ! FetchedAggregateRootEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion >= fromVersion): _*))
    case GetAggregateRootEventsTo(aggId, toVersion) ⇒
      sender() ! FetchedAggregateRootEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion <= toVersion): _*))
    case GetAggregateRootEventsUntil(aggId, untilVersion) ⇒
      sender() ! FetchedAggregateRootEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion < untilVersion): _*))
    case GetAggregateRootEventsFromTo(aggId, fromVersion, toVersion) ⇒
      sender() ! FetchedAggregateRootEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion >= fromVersion && event.aggVersion <= toVersion): _*))
    case GetAggregateRootEventsFromUntil(aggId, fromVersion, untilVersion) ⇒
      sender() ! FetchedAggregateRootEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion >= fromVersion && event.aggVersion < untilVersion): _*))
  }
}