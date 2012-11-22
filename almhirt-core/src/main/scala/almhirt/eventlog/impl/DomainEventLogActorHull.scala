package almhirt.eventlog.impl

import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import akka.util.duration._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.environment._
import almhirt.domain._
import almhirt.eventlog._
import akka.util.Duration

class DomainEventLogActorHull(val actor: ActorRef, onClose: () => Unit)(implicit almhirtContext: AlmhirtContext) extends DomainEventLog {
  private implicit def atMost = Duration(120, "seconds")
  private implicit def executionContext = almhirtContext.system.futureDispatcher

  def this(actor: ActorRef)(implicit almhirtContext: AlmhirtContext) = this(actor, () => ())
  
  def storeEvents(events: List[DomainEvent]) = (actor ? LogEventsQry(events, None))(atMost).mapTo[CommittedDomainEventsRsp].map(_.events)
  def purgeEvents(aggRootId: java.util.UUID) = AlmPromise { Nil.success }

  def getAllEvents() = (actor ? GetAllEventsQry)(atMost).mapTo[AllEventsRsp].map(x => x.chunk.events)
  def getEvents(id: UUID) = (actor ? GetEventsQry(id))(atMost).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long) = (actor ? GetEventsFromQry(id, fromVersion))(atMost).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (actor ? GetEventsFromToQry(id, fromVersion, toVersion))(atMost).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getRequiredNextEventVersion(id: UUID): AlmFuture[Long] = (actor ? GetRequiredNextEventVersionQry(id))(atMost).mapTo[RequiredNextEventVersionRsp].map(x => x.nextVersion)
  def close() { onClose() }
}