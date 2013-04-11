package almhirt.eventlog.impl

import java.util.{ UUID => JUUID }
import org.joda.time.DateTime
import scala.concurrent.duration.FiniteDuration
import scalaz.syntax.validation._
import akka.actor.ActorRef
import akka.pattern._
import almhirt.common._
import almhirt.almfuture.all._
import almhirt.core._
import almhirt.eventlog._
import com.typesafe.config.Config
import almhirt.environment.configuration._

class EventLogActorHull(val actor: ActorRef, maximumDirectCallDuration: FiniteDuration)(implicit hasExecutionContext: HasExecutionContext) extends EventLog {
  private implicit def executionContext = hasExecutionContext.executionContext

  def consume(event: Event) {
    actor ! LogEventQry(event, None)
  }

  def getAllEvents: AlmFuture[Iterable[Event]] =
    (actor ? GetAllEventsQry())(maximumDirectCallDuration).mapToSuccessfulAlmFuture[EventsRsp].mapV(x => x.chunk.events)
  def getEventById(eventId: JUUID): AlmFuture[Event] =
    (actor ? GetEventQry(eventId))(maximumDirectCallDuration).mapToSuccessfulAlmFuture[EventRsp].mapV(x => x.result)
  def getAllEventsFrom(from: DateTime): AlmFuture[Iterable[Event]] =
    (actor ? GetEventsFromQry(from))(maximumDirectCallDuration).mapToSuccessfulAlmFuture[EventsRsp].mapV(x => x.chunk.events)
  def getAllEventsUntil(until: DateTime): AlmFuture[Iterable[Event]] =
    (actor ? GetEventsUntilQry(until))(maximumDirectCallDuration).mapToSuccessfulAlmFuture[EventsRsp].mapV(x => x.chunk.events)
  def getAllEventsFromUntil(from: DateTime, until: DateTime): AlmFuture[Iterable[Event]] =
    (actor ? GetEventsFromUntilQry(from, until))(maximumDirectCallDuration).mapToSuccessfulAlmFuture[EventsRsp].mapV(x => x.chunk.events)
}

object EventLogActorHull {
  def apply(actor: ActorRef, config: Config)(implicit foundations: HasExecutionContext with HasDurations): EventLog = {
    val maxCallDuration =
      ConfigHelper.getMilliseconds(config)(ConfigPaths.eventlog + ".maximum_direct_call_duration")
        .getOrElse(foundations.durations.extraLongDuration)
    new EventLogActorHull(actor, maxCallDuration)
  }
}