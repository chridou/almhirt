package almhirt.eventlog.impl

import java.util.{ UUID => JUUID }
import scala.concurrent.duration.FiniteDuration
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

  def storeEvents(events: IndexedSeq[DomainEvent]): AlmFuture[(IndexedSeq[DomainEvent], Option[(Problem, IndexedSeq[DomainEvent])])] =
    (actor ? LogDomainEventsQry(events, None))(maximumDirectCallDuration).mapTo[LoggedDomainEventsRsp].toSuccessfulAlmFuture.map(rsp =>
      (rsp.committedEvents, rsp.uncommittedEvents))

  def getEventById(id: JUUID): AlmFuture[DomainEvent] = (actor ? GetDomainEventByIdQry(id))(maximumDirectCallDuration).mapTo[DomainEventByIdRsp].map(x => x.event)
  def getAllEvents() = (actor ? GetAllDomainEventsQry())(maximumDirectCallDuration).mapTo[AllDomainEventsRsp].map(x => x.chunk.events)
  def getAllEventsFor(aggId: JUUID) = (actor ? GetDomainEventsQry(aggId))(maximumDirectCallDuration).mapTo[DomainEventsForAggregateRootRsp].map(x => x.chunk.events)
  def getAllEventsForFrom(aggId: JUUID, fromVersion: Long) = (actor ? GetDomainEventsFromQry(aggId, fromVersion))(maximumDirectCallDuration).mapTo[DomainEventsForAggregateRootRsp].map(x => x.chunk.events)
  def getAllEventsForTo(aggId: JUUID, toVersion: Long) = (actor ? GetDomainEventsToQry(aggId, toVersion))(maximumDirectCallDuration).mapTo[DomainEventsForAggregateRootRsp].map(x => x.chunk.events)
  def getAllEventsForFromTo(aggId: JUUID, fromVersion: Long, toVersion: Long) = (actor ? GetDomainEventsFromToQry(aggId, fromVersion, toVersion))(maximumDirectCallDuration).mapTo[DomainEventsForAggregateRootRsp].map(x => x.chunk.events)
}

object DomainEventLogActorHull {
  def apply(actor: ActorRef, config: Config)(implicit foundations: HasExecutionContext with HasDurations): DomainEventLog = {
    val maxCallDuration =
      ConfigHelper.getMilliseconds(config)(ConfigPaths.domaineventlog + ".maximum_direct_call_duration")
        .getOrElse(foundations.durations.extraLongDuration)
    DomainEventLogActorHull(actor, maxCallDuration)
  }

  def apply(actor: ActorRef, maxDirectCallDuration: FiniteDuration)(implicit hasExecutionContext: HasExecutionContext): DomainEventLog = {
    new DomainEventLogActorHull(actor, maxDirectCallDuration)
  }

}