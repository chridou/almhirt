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

class DomainEventLogActorHull(val actor: ActorRef, onClose: () => Unit, maximumDirectCallDuration: Duration)(implicit almhirtContext: AlmhirtContext) extends DomainEventLog {
  private implicit def executionContext = almhirtContext.system.futureDispatcher

  def this(actor: ActorRef, onClose: Option[() => Unit], maximumDirectCallDuration: Option[Duration])(implicit almhirtContext: AlmhirtContext) = this(actor, onClose.getOrElse(() => ()), maximumDirectCallDuration.getOrElse(Duration(5, "seconds")))
  def this(actor: ActorRef, onClose: () => Unit, maximumDirectCallDuration: Option[Duration])(implicit almhirtContext: AlmhirtContext) = this(actor, Some(onClose), maximumDirectCallDuration)
  def this(actor: ActorRef, onClose: Option[() => Unit])(implicit almhirtContext: AlmhirtContext) = this(actor, onClose, None)
  def this(actor: ActorRef)(implicit almhirtContext: AlmhirtContext) = this(actor, None, None)
  
  def storeEvents(events: List[DomainEvent]) = (actor ? LogEventsQry(events, None))(maximumDirectCallDuration).mapTo[CommittedDomainEventsRsp].map(_.events)
  def purgeEvents(aggRootId: java.util.UUID) = AlmPromise { Nil.success }

  def getAllEvents() = (actor ? GetAllEventsQry)(maximumDirectCallDuration).mapTo[AllEventsRsp].map(x => x.chunk.events)
  def getEvents(id: UUID) = (actor ? GetEventsQry(id))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long) = (actor ? GetEventsFromQry(id, fromVersion))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (actor ? GetEventsFromToQry(id, fromVersion, toVersion))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getRequiredNextEventVersion(id: UUID): AlmFuture[Long] = (actor ? GetRequiredNextEventVersionQry(id))(maximumDirectCallDuration).mapTo[RequiredNextEventVersionRsp].map(x => x.nextVersion)
  def close() { onClose() }
}