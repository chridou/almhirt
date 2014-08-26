package almhirt.eventlog

import scalaz.syntax.validation._
import akka.actor._
import almhirt.common._
import almhirt.configuration._
import play.api.libs.iteratee.Enumerator

object InMemoryAggregateEventLog {
  def props(): Props = 
    Props(new InMemoryAggregateEventLog())
}

class InMemoryAggregateEventLog extends Actor with ActorLogging {
  import AggregateEventLog._

  private var domainEventLog = Vector.empty[AggregateRootEvent]

  def receive: Receive = {
    case CommitAggregateEvent(event) ⇒
      domainEventLog = domainEventLog :+ event
      sender() ! AggregateEventCommitted(event.eventId)
    case GetAllAggregateEvents ⇒
      sender() ! FetchedAggregateEvents(Enumerator(domainEventLog: _*))
    case GetAggregateEvent(eventId) ⇒
      sender() ! FetchedAggregateEvent(eventId, domainEventLog.find(_.eventId == eventId))
    case GetAllAggregateEventsFor(aggId) ⇒
      sender() ! FetchedAggregateEvents(Enumerator(domainEventLog.filter(_.aggId == aggId): _*))
    case GetAggregateEventsFrom(aggId, fromVersion) ⇒
      sender() ! FetchedAggregateEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion >= fromVersion): _*))
    case GetAggregateEventsTo(aggId, toVersion) ⇒
      sender() ! FetchedAggregateEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion <= toVersion): _*))
    case GetAggregateEventsUntil(aggId, untilVersion) ⇒
      sender() ! FetchedAggregateEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion < untilVersion): _*))
    case GetAggregateEventsFromTo(aggId, fromVersion, toVersion) ⇒
      sender() ! FetchedAggregateEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion >= fromVersion && event.aggVersion <= toVersion): _*))
    case GetAggregateEventsFromUntil(aggId, fromVersion, untilVersion) ⇒
      sender() ! FetchedAggregateEvents(Enumerator(domainEventLog.filter(event ⇒ event.aggId == aggId && event.aggVersion >= fromVersion && event.aggVersion < untilVersion): _*))
  }
}