package almhirt.components

import scala.language.postfixOps
import scala.concurrent.duration._
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import akka.pattern._
import almhirt.common._
import almhirt.almvalidation.kit._
import almhirt.almfuture.all._
import almhirt.akkax._
import almhirt.context.AlmhirtContext
import almhirt.context.HasAlmhirtContext
import almhirt.streaming.ActorDevNullSubscriberWithAutoSubscribe
import akka.stream.actor._
import org.reactivestreams.Subscriber
import akka.stream.scaladsl._

object EventLogWriter {
  def propsRaw(
    eventLogToResolve: ToResolve,
    resolveSettings: ResolveSettings,
    warningThreshold: FiniteDuration,
    circuitControlSettings: CircuitControlSettings,
    circuitStateReportingInterval: Option[FiniteDuration],
    eventlogCallTimeout: FiniteDuration,
    storeEventRetrySettings: RetryPolicyExt,
    autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props = {
    Props(new EventLogWriterImpl(
      eventLogToResolve,
      resolveSettings,
      warningThreshold,
      autoConnect,
      circuitControlSettings,
      circuitStateReportingInterval,
      eventlogCallTimeout,
      storeEventRetrySettings))
  }

  def props(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    val eventlogPath = ctx.localActorPaths.eventLogs / almhirt.eventlog.EventLog.actorname
    for {
      section ← ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.event-sink-hub.event-publishers.event-log-writer")
      enabled ← section.v[Boolean]("enabled")
      autoConnect ← section.v[Boolean]("auto-connect")
      res ← if (enabled) {
        for {
          warningThreshold ← section.v[FiniteDuration]("warning-threshold")
          resolveSettings ← section.v[ResolveSettings]("resolve-settings")
          circuitControlSettings ← section.v[CircuitControlSettings]("circuit-control")
          circuitStateReportingInterval ← section.magicOption[FiniteDuration]("circuit-state-reporting-interval")
          storeEventRetrySettings ← section.v[RetryPolicyExt]("store-event-retry-settings")
          eventlogCallTimeout ← section.v[FiniteDuration]("event-log-call-timeout")
        } yield propsRaw(ResolvePath(eventlogPath), resolveSettings, warningThreshold, circuitControlSettings, circuitStateReportingInterval, eventlogCallTimeout, storeEventRetrySettings, autoConnect)
      } else {
        ActorDevNullSubscriberWithAutoSubscribe.props[Event](1, if (autoConnect) Some(ctx.eventStream) else None).success
      }
    } yield res
  }

  def apply(eventLogWriter: ActorRef): Subscriber[Event] =
    ActorSubscriber[Event](eventLogWriter)

  val actorname = "event-log-writer"
  def path(root: RootActorPath) = EventSinkHub.path(root) / actorname
}

private[almhirt] class EventLogWriterImpl(
    eventLogToResolve: ToResolve,
    resolveSettings: ResolveSettings,
    warningThreshold: FiniteDuration,
    autoConnect: Boolean,
    circuitControlSettings: CircuitControlSettings,
    circuitStateReportingInterval: Option[FiniteDuration],
    eventlogCallTimeout: FiniteDuration,
    storeEventRetrySettings: RetryPolicyExt)(implicit override val almhirtContext: AlmhirtContext) extends ActorSubscriber with AlmActor with AlmActorLogging with ActorLogging with ControllableActor {
  import almhirt.eventlog.EventLog

  override val supportedComponentControlActions = ActorMessages.ComponentControlActions.none

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)
  implicit val executor = almhirtContext.futuresContext

  override val requestStrategy = ZeroRequestStrategy

  val circuitBreaker = AlmCircuitBreaker(circuitControlSettings, almhirtContext.futuresContext, context.system.scheduler)

  private case object AutoConnect
  private case object Resolve
  private case object DisplayCircuitState

  def receiveResolve: Receive = startup() {
    case Resolve ⇒
      registerComponentControl()
      context.resolveSingle(eventLogToResolve, resolveSettings, None, Some("event-log-resolver"))

    case ActorMessages.ResolvedSingle(eventlog, _) ⇒
      logInfo("Found event log.")

      registerCircuitControl(circuitBreaker)

      if (autoConnect)
        self ! AutoConnect
      else
        request(1)
      context.become(receiveCircuitClosed(eventlog))

    case ActorMessages.SingleNotResolved(problem, _) ⇒
      logError(s"Could not resolve event log @${eventLogToResolve}:\n$problem")
      sys.error(s"Could not resolve event log @${eventLogToResolve}.")
      reportCriticalFailure(problem)
  }

  def receiveCircuitClosed(eventLog: ActorRef): Receive = running() {
    case AutoConnect ⇒
      logInfo("Subscribing to event stream.")
      Source(almhirtContext.eventStream).to(Sink(EventLogWriter(self))).run()
      request(1)

    case ActorSubscriberMessage.OnNext(event: Event) ⇒
      if (!event.header.noLoggingSuggested) {
        val start = Deadline.now
        val f = circuitBreaker.fused {
          this.retryFuture(storeEventRetrySettings) {
            (eventLog ? EventLog.LogEvent(event, true))(eventlogCallTimeout).mapCastTo[EventLog.LogEventResponse]
          }
        }.onComplete({
          case scalaz.Failure(problem) ⇒
            self ! EventLog.EventNotLogged(event.eventId, problem)
            reportMissedEvent(event, MajorSeverity, problem)
            reportMajorFailure(problem)
          case scalaz.Success(rsp) ⇒ self ! rsp
        })

        f.onSuccess(rsp ⇒
          if (start.lapExceeds(warningThreshold))
            logWarning(s"Writing event '${event.eventId.value}' took longer than ${warningThreshold.defaultUnitString}: ${start.lap.defaultUnitString}"))
      } else {
        request(1)
      }

    case ActorSubscriberMessage.OnNext(unprocessable) ⇒
      log.warning(s"Received unprocessable element $unprocessable.")
      request(1)

    case EventLog.EventLogged(id) ⇒
      request(1)

    case EventLog.EventNotLogged(id, problem) ⇒
      logError(s"Could not log event '${id.value}':\n$problem")
      reportMajorFailure(problem)
      request(1)

    case m: ActorMessages.CircuitAllWillFail ⇒
      context.become(receiveCircuitOpen(eventLog))
      self ! DisplayCircuitState

    case DisplayCircuitState ⇒
      if (log.isInfoEnabled)
        circuitBreaker.state.onSuccess(s ⇒ log.info(s"Circuit state: $s"))
  }

  def receiveCircuitOpen(eventLog: ActorRef): Receive = running() {
    case ActorSubscriberMessage.OnNext(event: Event) ⇒
      reportMissedEvent(event, MajorSeverity, CircuitOpenProblem())
      request(1)

    case EventLog.EventLogged(id) ⇒
      request(1)

    case EventLog.EventNotLogged(id, problem) ⇒
      logError(s"Could not log event '${id.value}':\n$problem")
      reportMajorFailure(problem)
      request(1)

    case m: ActorMessages.CircuitNotAllWillFail ⇒
      context.become(receiveCircuitClosed(eventLog))
      self ! DisplayCircuitState

    case DisplayCircuitState ⇒
      if (log.isInfoEnabled) {
        circuitBreaker.state.onSuccess(s ⇒ logInfo(s"Circuit state: $s"))
        circuitStateReportingInterval.foreach(interval ⇒
          context.system.scheduler.scheduleOnce(interval, self, DisplayCircuitState))
      }
  }

  override def receive: Receive = receiveResolve

  override def preStart() {
    circuitBreaker.defaultActorListeners(self)
      .onWarning((n, max) ⇒ logWarning(s"$n failures in a row. $max will cause the circuit to open."))

    self ! Resolve
  }

  override def postStop() {
    deregisterComponentControl()
    deregisterCircuitControl()
  }
} 