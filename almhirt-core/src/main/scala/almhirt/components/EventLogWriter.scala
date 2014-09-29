package almhirt.components

import scalaz._, Scalaz._
import scala.language.postfixOps
import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.akkax._
import almhirt.context.AlmhirtContext
import akka.stream.actor._
import org.reactivestreams.Subscriber
import akka.stream.scaladsl2._

object EventLogWriter {
  def propsRaw(
    eventLogToResolve: ToResolve,
    resolveSettings: ResolveSettings,
    maxWaitForEventLog: FiniteDuration,
    autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props = {
    Props(new EventLogWriterImpl(
      eventLogToResolve,
      resolveSettings,
      maxWaitForEventLog: FiniteDuration,
      autoConnect))
  }

  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    for {
      section <- ctx.config.v[com.typesafe.config.Config]("almhirt.components.event-log-writer")
      eventLogPathStr <- section.v[String]("event-log-path")
      eventLogToResolve <- inTryCatch { ResolvePath(ActorPath.fromString(eventLogPathStr)) }
      maxWaitForEventLog <- section.v[FiniteDuration]("max-wait-for-event-log")
      resolveSettings <- section.v[ResolveSettings]("resolve-settings")
      autoConnect <- section.v[Boolean]("auto-connect")
    } yield propsRaw(eventLogToResolve, resolveSettings, maxWaitForEventLog, autoConnect)
  }

  def apply(eventLogWriter: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](eventLogWriter)
    
  val actorname = "event-log-writer"
}

private[almhirt] class EventLogWriterImpl(
  eventLogToResolve: ToResolve,
  resolveSettings: ResolveSettings,
  maxWaitForEventLog: FiniteDuration,
  autoConnect: Boolean)(implicit ctx: AlmhirtContext) extends ActorSubscriber with ActorLogging with ImplicitFlowMaterializer {
  import almhirt.eventlog.EventLog

  override val requestStrategy = ZeroRequestStrategy

  private case class PotentialTimeout(eventId: EventId)
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
      context.become(running(eventlog, None, cancelTimeout = None))

    case ActorMessages.SingleNotResolved(problem, _) ⇒
      log.error(s"Could not resolve event log @ ${eventLogToResolve}:\n$problem")
      sys.error(s"Could not resolve event log @ ${eventLogToResolve}.")
  }

  
  def running(eventLog: ActorRef, activeEvent: Option[Event], cancelTimeout: Option[Cancellable]): Receive = {
    case AutoConnect ⇒
      log.info("Subscribing to event stream.")
      FlowFrom(ctx.eventStream).publishTo(EventLogWriter(self))
      request(1)

    case ActorSubscriberMessage.OnNext(event: Event) ⇒
      if (activeEvent.isDefined) {
        sys.error(s"Only one event may be processed at any time. Currently event with id '${activeEvent.get.eventId.value}' is processed.")
      }

      val cancel = context.system.scheduler.scheduleOnce(maxWaitForEventLog, self, PotentialTimeout(event.eventId))(context.dispatcher)
      eventLog ! EventLog.LogEvent(event)
      context.become(running(eventLog, activeEvent = Some(event), Some(cancel)))

    case EventLog.EventLogged(id) ⇒
      if (activeEvent.map(_.eventId == id) | false) {
        request(1)
        cancelTimeout.foreach(_.cancel())
        context.become(running(eventLog, activeEvent = None, cancelTimeout = None))
      } else {
        log.warning(s"Received event logged for event '${id.value}' which is not the active event.")
      }

    case EventLog.EventNotLogged(id, problem) ⇒
      if (activeEvent.map(_.eventId == id) | false) {
        log.error(s"Could not log event '${id.value}':\n$problem")
        request(1)
        cancelTimeout.foreach(_.cancel())
        context.become(running(eventLog, activeEvent = None, cancelTimeout = None))
      } else {
        log.error(s"Received event not logged for event '${id.value}' which is not the active event:\n$problem")
      }

    case PotentialTimeout(eventId) ⇒
      if (activeEvent.map(_.eventId == eventId) | false) {
        log.warning(s"Writing event '${eventId.value}' timed out. It might have been written or not...")
        request(1)
        context.become(running(eventLog, activeEvent = None, cancelTimeout = None))
      } else {
        log.warning(s"Received timeout for event '${eventId.value}' which is not the active event.")
      }

    case ActorSubscriberMessage.OnNext(unprocessable) ⇒
      if (activeEvent.isDefined) {
        sys.error(s"Only one event may be processed at any time. Currently event with id '${activeEvent.get.eventId.value}' is processed.")
      } else {
        log.warning(s"received unprocessable element $unprocessable")
        request(1)
      }
  }

  override def receive: Receive = receiveResolve

  override def preStart() {
    self ! Resolve
  }
} 