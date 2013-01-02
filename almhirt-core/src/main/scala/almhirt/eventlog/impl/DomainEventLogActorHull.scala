package almhirt.eventlog.impl

import scala.concurrent.duration.FiniteDuration
import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.environment._
import almhirt.domain._
import almhirt.eventlog._

class DomainEventLogActorHull(val actor: ActorRef, onClose: () => Unit, maximumDirectCallDuration: FiniteDuration)(implicit theAlmhirt: Almhirt) extends DomainEventLog {
  private implicit def executionContext = theAlmhirt.executionContext

  def this(actor: ActorRef, onClose: Option[() => Unit], maximumDirectCallDuration: Option[FiniteDuration])(implicit theAlmhirt: Almhirt) = this(actor, onClose.getOrElse(() => ()), maximumDirectCallDuration.getOrElse(FiniteDuration(5, "seconds")))
  def this(actor: ActorRef, onClose: () => Unit, maximumDirectCallDuration: Option[FiniteDuration])(implicit theAlmhirt: Almhirt) = this(actor, Some(onClose), maximumDirectCallDuration)
  def this(actor: ActorRef, onClose: Option[() => Unit])(implicit theAlmhirt: Almhirt) = this(actor, onClose, None)
  def this(actor: ActorRef)(implicit theAlmhirt: Almhirt) = this(actor, None, None)
  
  def storeEvents(events: List[DomainEvent]) = (actor ? LogEventsQry(events, None))(maximumDirectCallDuration).mapTo[CommittedDomainEventsRsp].map(_.events)
  def purgeEvents(aggRootId: java.util.UUID) = AlmFuture.successful{ Nil }

  def getAllEvents() = (actor ? GetAllEventsQry())(maximumDirectCallDuration).mapTo[AllEventsRsp].map(x => x.chunk.events)
  def getEvents(id: UUID) = (actor ? GetEventsQry(id))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long) = (actor ? GetEventsFromQry(id, fromVersion))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (actor ? GetEventsFromToQry(id, fromVersion, toVersion))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getRequiredNextEventVersion(id: UUID): AlmFuture[Long] = (actor ? GetRequiredNextEventVersionQry(id))(maximumDirectCallDuration).mapTo[RequiredNextEventVersionRsp].map(x => x.nextVersion)
  def close() { onClose() }
}