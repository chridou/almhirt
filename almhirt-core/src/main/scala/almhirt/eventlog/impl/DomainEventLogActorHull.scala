package almhirt.eventlog.impl

import scala.concurrent.duration.FiniteDuration
import java.util.UUID
import scalaz.syntax.validation._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.core._
import almhirt.almfuture.all._
import almhirt.environment._
import almhirt.domain._
import almhirt.eventlog._
import almhirt.environment.configuration._
import com.typesafe.config.Config

class DomainEventLogActorHull(val actor: ActorRef, maximumDirectCallDuration: FiniteDuration)(implicit hasExecutionContext: HasExecutionContext) extends DomainEventLog {
  private implicit def executionContext = hasExecutionContext.executionContext

  def this(actor: ActorRef, maximumDirectCallDuration: Option[FiniteDuration])(implicit theAlmhirt: Almhirt) = this(actor, maximumDirectCallDuration.getOrElse(FiniteDuration(5, "seconds")))
  def this(actor: ActorRef)(implicit theAlmhirt: Almhirt) = this(actor, None)

  def storeEvents(events: IndexedSeq[DomainEvent]): AlmFuture[(IndexedSeq[DomainEvent], Option[(Problem,IndexedSeq[DomainEvent])])] =
    (actor ? LogEventsQry(events, None))(maximumDirectCallDuration).mapTo[LoggedDomainEventsRsp].toSuccessfulAlmFuture.map (rsp =>
      (rsp.committedEvents, rsp.uncommittedEvents))

  def getAllEvents() = (actor ? GetAllEventsQry())(maximumDirectCallDuration).mapTo[AllEventsRsp].map(x => x.chunk.events)
  def getEvents(id: UUID) = (actor ? GetEventsQry(id))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long) = (actor ? GetEventsFromQry(id, fromVersion))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
  def getEvents(id: UUID, fromVersion: Long, toVersion: Long) = (actor ? GetEventsFromToQry(id, fromVersion, toVersion))(maximumDirectCallDuration).mapTo[EventsForAggregateRootRsp].map(x => x.chunk.events)
}

object DomainEventLogActorHull {
  def apply(actor: ActorRef, config: Config)(implicit foundations: HasExecutionContext with HasDurations): DomainEventLog = {
    val maxCallDuration =
      ConfigHelper.getMilliseconds(config)(ConfigPaths.eventlog + ".maximum_direct_call_duration")
        .getOrElse(foundations.durations.extraLongDuration)
    DomainEventLogActorHull(actor, maxCallDuration)
  }

  def apply(actor: ActorRef, maxDirectCallDuration: FiniteDuration)(implicit hasExecutionContext: HasExecutionContext): DomainEventLog = {
    new DomainEventLogActorHull(actor, maxDirectCallDuration)
  }

}