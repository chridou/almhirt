package almhirt.components

import scalaz._, Scalaz._
import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.akkax._
import almhirt.context.AlmhirtContext
import almhirt.streaming.ActorDevNullSubscriberWithAutoSubscribe
import akka.stream.actor._
import org.reactivestreams.Subscriber
import akka.stream.scaladsl2._

object EventLogWriter {
  def propsRaw(
    eventLogToResolve: ToResolve,
    resolveSettings: ResolveSettings,
    warningThreshold: FiniteDuration,
    autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props = {
    Props(new EventLogWriterImpl(
      eventLogToResolve,
      resolveSettings,
      warningThreshold,
      autoConnect))
  }

  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    for {
      section <- ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.event-sink-hub.event-publishers.event-log-writer")
      enabled <- section.v[Boolean]("enabled")
      autoConnect <- section.v[Boolean]("auto-connect")
      res <- if (enabled) {
        for {
          eventLogPathStr <- section.v[String]("event-log-path")
          eventLogToResolve <- inTryCatch { ResolvePath(ActorPath.fromString(eventLogPathStr)) }
          warningThreshold <- section.v[FiniteDuration]("warning-threshold")
          resolveSettings <- section.v[ResolveSettings]("resolve-settings")
        } yield propsRaw(eventLogToResolve, resolveSettings, warningThreshold, autoConnect)
      } else {
        ActorDevNullSubscriberWithAutoSubscribe.props[Event](1, if(autoConnect) Some(ctx.eventStream) else None).success
      }
    } yield res
  }

  def apply(eventLogWriter: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](eventLogWriter)

  val actorname = "event-log-writer"
}

private[almhirt] class EventLogWriterImpl(
  eventLogToResolve: ToResolve,
  resolveSettings: ResolveSettings,
  warningThreshold: FiniteDuration,
  autoConnect: Boolean)(implicit ctx: AlmhirtContext) extends ActorSubscriber with ActorLogging with ImplicitFlowMaterializer {
  import almhirt.eventlog.EventLog

  override val requestStrategy = ZeroRequestStrategy

  private case object AutoConnect

  private case object Resolve
  def receiveResolve: Receive = {
    case Resolve ⇒
      context.resolveSingle(eventLogToResolve, resolveSettings, None, Some("event-log-resolver"))

    case ActorMessages.ResolvedSingle(eventlog, _) ⇒
      log.info("Found event log.")
      if (autoConnect)
        self ! AutoConnect
      else
        request(1)
      context.become(receiveWaiting(eventlog))

    case ActorMessages.SingleNotResolved(problem, _) ⇒
      log.error(s"Could not resolve event log @ ${eventLogToResolve}:\n$problem")
      sys.error(s"Could not resolve event log @ ${eventLogToResolve}.")
  }

  def receiveWaiting(eventLog: ActorRef): Receive = {
    case AutoConnect ⇒
      log.info("Subscribing to event stream.")
      FlowFrom(ctx.eventStream).publishTo(EventLogWriter(self))
      request(1)

    case ActorSubscriberMessage.OnNext(event: Event) ⇒
      eventLog ! EventLog.LogEvent(event, true)
      context.become(receiveWriting(eventLog, event, Deadline.now))

    case ActorSubscriberMessage.OnNext(unprocessable) ⇒
      log.warning(s"received unprocessable element $unprocessable")
      request(1)
  }

  def receiveWriting(eventLog: ActorRef, activeEvent: Event, start: Deadline): Receive = {
    case ActorSubscriberMessage.OnNext(event) ⇒
      sys.error(s"Only one event may be processed at any time. Currently event with id '${activeEvent.eventId.value}' is processed.")

    case EventLog.EventLogged(id) ⇒
      if (start.lapExceeds(warningThreshold))
        log.warning(s"Wrinting event '${id.value}' took longer than ${warningThreshold.defaultUnitString}: ${start.lap.defaultUnitString}")
      request(1)
      context.become(receiveWaiting(eventLog))

    case EventLog.EventNotLogged(id, problem) ⇒
      log.error(s"Could not log event '${id.value}' after ${start.lap.defaultUnitString}:\n$problem")
      request(1)
      context.become(receiveWaiting(eventLog))

  }

  override def receive: Receive = receiveResolve

  override def preStart() {
    self ! Resolve
  }
} 