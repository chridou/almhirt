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
import almhirt.environment.configuration._

class DomainEventLogActorHull(val actor: ActorRef, maximumDirectCallDuration: FiniteDuration)(implicit hasExecutionContext: HasExecutionContext) extends DomainEventLog {
  private implicit def executionContext = hasExecutionContext.executionContext

  def this(actor: ActorRef, maximumDirectCallDuration: Option[FiniteDuration])(implicit theAlmhirt: Almhirt) = this(actor, maximumDirectCallDuration.getOrElse(FiniteDuration(5, "seconds")))
  def this(actor: ActorRef)(implicit theAlmhirt: Almhirt) = this(actor, None)
  
  def storeEvents(events: List[DomainEvent]) = (actor ? LogEventsQry(events, None))(maximumDirectCallDuration).mapTo[CommittedDomainEventsRsp].map(_.events)
  def purgeEvents(aggRootId: java.util.UUID) = AlmFuture.successful{ Nil }

  def getAllEvents() = (actor ? GetAllEventsQry())(maximumDirectCallDuration).mapTo[AllEventsRsp].map(x => x.chunk.events)
  def getEvents(id: UUID) = (actor ? GetEventsQry(id))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long) = (actor ? GetEventsFromQry(id, fromVersion))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (actor ? GetEventsFromToQry(id, fromVersion, toVersion))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getRequiredNextEventVersion(id: UUID): AlmFuture[Long] = (actor ? GetRequiredNextEventVersionQry(id))(maximumDirectCallDuration).mapTo[RequiredNextEventVersionRsp].map(x => x.nextVersion)
}

object DomainEventLogActorHull {
  def apply(actor: ActorRef)(implicit theAlmhirt: Almhirt): DomainEventLog = {
    val maxCallDuration = ConfigHelper.tryGetDuration(theAlmhirt.system.config)(ConfigPaths.eventlog+".maximum_direct_call_duration")
    new DomainEventLogActorHull(actor, maxCallDuration)
  }

  def apply(actor: ActorRef, maxDirectCallDuration: FiniteDuration)(implicit hasExecutionContext: HasExecutionContext): DomainEventLog = {
    new DomainEventLogActorHull(actor, maxDirectCallDuration)
  }

}