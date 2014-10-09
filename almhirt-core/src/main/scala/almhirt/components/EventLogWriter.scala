package almhirt.components

import scalaz._, Scalaz._
import scala.language.postfixOps
import scala.concurrent.duration._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
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
    circuitBreakerSettings: AlmCircuitBreaker.AlmCircuitBreakerSettings,
    circuitBreakerStateReportingInterval: Option[FiniteDuration],
    autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props = {
    Props(new EventLogWriterImpl(
      eventLogToResolve,
      resolveSettings,
      warningThreshold,
      autoConnect,
      circuitBreakerSettings,
      circuitBreakerStateReportingInterval))
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
          circuitBreakerSettings <- section.v[AlmCircuitBreaker.AlmCircuitBreakerSettings]("circuit-breaker")
          circuitBreakerStateReportingInterval <- section.magicOption[FiniteDuration]("circuit-breaker-state-reporting-interval")
        } yield propsRaw(eventLogToResolve, resolveSettings, warningThreshold, circuitBreakerSettings, circuitBreakerStateReportingInterval, autoConnect)
      } else {
        ActorDevNullSubscriberWithAutoSubscribe.props[Event](1, if (autoConnect) Some(ctx.eventStream) else None).success
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
  autoConnect: Boolean,
  circuitBreakerSettings: AlmCircuitBreaker.AlmCircuitBreakerSettings,
  circuitBreakerStateReportingInterval: Option[FiniteDuration])(implicit ctx: AlmhirtContext) extends ActorSubscriber with ActorLogging with ImplicitFlowMaterializer {
  import almhirt.eventlog.EventLog

  implicit val executor = ctx.futuresContext

  override val requestStrategy = ZeroRequestStrategy

  val circuitBreakerParams =
    AlmCircuitBreaker.AlmCircuitBreakerParams(
      settings = circuitBreakerSettings,
      onOpened = Some(() => self ! ActorMessages.CircuitOpened),
      onHalfOpened = Some(() => self ! ActorMessages.CircuitHalfOpened),
      onClosed = Some(() => self ! ActorMessages.CircuitClosed),
      onWarning = Some((n, max) => log.warning(s"$n failures in a row. $max will cause the circuit to open.")))

  val circuitBreaker = AlmCircuitBreaker(circuitBreakerParams, context.dispatcher, context.system.scheduler)

  private case object AutoConnect
  private case object Resolve
  private case object DisplayCircuitState

  def receiveResolve: Receive = {
    case Resolve ⇒
      context.resolveSingle(eventLogToResolve, resolveSettings, None, Some("event-log-resolver"))

    case ActorMessages.ResolvedSingle(eventlog, _) ⇒
      log.info("Found event log.")
      if (autoConnect)
        self ! AutoConnect
      else
        request(1)
      context.become(receiveCircuitClosed(eventlog))

    case ActorMessages.SingleNotResolved(problem, _) ⇒
      log.error(s"Could not resolve event log @ ${eventLogToResolve}:\n$problem")
      sys.error(s"Could not resolve event log @ ${eventLogToResolve}.")

    case ActorMessages.ReportCircuitBreakerState(id) =>
      sender() ! ActorMessages.CurrentCircuitBreakerState(id, circuitBreaker.state)
      
    case ActorMessages.AttemptResetCircuitBreaker =>
      if(circuitBreaker.reset())
        log.info("Manual reset attempt successful.")
      else
        log.info("Manual reset attempt caused no change.")
  }

  def receiveCircuitClosed(eventLog: ActorRef): Receive = {
    case AutoConnect ⇒
      log.info("Subscribing to event stream.")
      FlowFrom(ctx.eventStream).publishTo(EventLogWriter(self))
      request(1)

    case ActorSubscriberMessage.OnNext(event: Event) ⇒
      val start = Deadline.now
      val f = (eventLog ? EventLog.LogEvent(event, true))(circuitBreakerSettings.callTimeout).mapCastTo[EventLog.LogEventResponse]
      circuitBreaker.fused(f).onComplete({
        case scalaz.Failure(problem) => self ! EventLog.EventNotLogged(event.eventId, problem)
        case scalaz.Success(rsp) => self ! rsp
      })

      f.onSuccess(rsp =>
        if (start.lapExceeds(warningThreshold))
          log.warning(s"Wrinting event '${event.eventId.value}' took longer than ${warningThreshold.defaultUnitString}: ${start.lap.defaultUnitString}"))

    case ActorSubscriberMessage.OnNext(unprocessable) ⇒
      log.warning(s"Received unprocessable element $unprocessable.")
      request(1)

    case EventLog.EventLogged(id) ⇒
      request(1)

    case EventLog.EventNotLogged(id, problem) ⇒
      log.error(s"Could not log event '${id.value}':\n$problem")
      request(1)

    case ActorMessages.CircuitOpened =>
      log.warning("Circuit opened")
      context.become(receiveCircuitOpen(eventLog))
      self ! DisplayCircuitState

    case DisplayCircuitState =>
      if (log.isInfoEnabled)
        log.info(s"Circuit state: s{circuitBreaker.state}")

    case ActorMessages.ReportCircuitBreakerState(id) =>
      sender() ! ActorMessages.CurrentCircuitBreakerState(id, circuitBreaker.state)
      
    case ActorMessages.AttemptResetCircuitBreaker =>
      if(circuitBreaker.reset())
        log.info("Manual reset attempt successful.")
      else
        log.info("Manual reset attempt caused no change.")
  }

  def receiveCircuitOpen(eventLog: ActorRef): Receive = {
    case ActorSubscriberMessage.OnNext(element) ⇒
      request(1)

    case EventLog.EventLogged(id) ⇒
      request(1)

    case EventLog.EventNotLogged(id, problem) ⇒
      log.error(s"Could not log event '${id.value}':\n$problem")
      request(1)

    case ActorMessages.CircuitClosed =>
      if (log.isInfoEnabled)
        log.info("Circuit closed")
      context.become(receiveCircuitClosed(eventLog))
      self ! DisplayCircuitState

    case ActorMessages.CircuitHalfOpened =>
      if (log.isInfoEnabled)
        log.info("Circuit half opend")
      context.become(receiveCircuitClosed(eventLog))
      self ! DisplayCircuitState

    case DisplayCircuitState =>
      if (log.isInfoEnabled) {
        log.info(s"Circuit state: s{circuitBreaker.state}")
        circuitBreakerStateReportingInterval.foreach(interval =>
          context.system.scheduler.scheduleOnce(interval, self, DisplayCircuitState))
      }

    case ActorMessages.ReportCircuitBreakerState(id) =>
      sender() ! ActorMessages.CurrentCircuitBreakerState(id, circuitBreaker.state)
      
    case ActorMessages.AttemptResetCircuitBreaker =>
      if(circuitBreaker.reset())
        log.info("Manual reset attempt successful.")
      else
        log.info("Manual reset attempt caused no change.")
  }

  override def receive: Receive = receiveResolve

  override def preStart() {
    self ! Resolve
  }
} 