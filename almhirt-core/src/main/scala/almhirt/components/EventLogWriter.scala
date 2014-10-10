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
import almhirt.context.HasAlmhirtContext
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
  def path(root: RootActorPath) = EventSinkHub.path(root) / actorname
}

private[almhirt] class EventLogWriterImpl(
  eventLogToResolve: ToResolve,
  resolveSettings: ResolveSettings,
  warningThreshold: FiniteDuration,
  autoConnect: Boolean,
  circuitBreakerSettings: AlmCircuitBreaker.AlmCircuitBreakerSettings,
  circuitBreakerStateReportingInterval: Option[FiniteDuration])(implicit override val almhirtContext: AlmhirtContext) extends ActorSubscriber with HasAlmhirtContext with ActorLogging with ImplicitFlowMaterializer {
  import almhirt.eventlog.EventLog

  implicit val executor = almhirtContext.futuresContext

  override val requestStrategy = ZeroRequestStrategy

  val circuitBreaker = AlmCircuitBreaker(circuitBreakerSettings, almhirtContext.futuresContext, context.system.scheduler)

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
  }

  def receiveCircuitClosed(eventLog: ActorRef): Receive = {
    case AutoConnect ⇒
      log.info("Subscribing to event stream.")
      FlowFrom(almhirtContext.eventStream).publishTo(EventLogWriter(self))
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

    case m: ActorMessages.CircuitBreakerAllWillFail =>
      context.become(receiveCircuitOpen(eventLog))
      self ! DisplayCircuitState

    case DisplayCircuitState =>
      if (log.isInfoEnabled)
        log.info(s"Circuit state: ${circuitBreaker.state}")
  }

  def receiveCircuitOpen(eventLog: ActorRef): Receive = {
    case ActorSubscriberMessage.OnNext(element) ⇒
      request(1)

    case EventLog.EventLogged(id) ⇒
      request(1)

    case EventLog.EventNotLogged(id, problem) ⇒
      log.error(s"Could not log event '${id.value}':\n$problem")
      request(1)

    case m: ActorMessages.CircuitBreakerNotAllWillFail =>
      context.become(receiveCircuitClosed(eventLog))
      self ! DisplayCircuitState

    case DisplayCircuitState =>
      if (log.isInfoEnabled) {
        log.info(s"Circuit state: ${circuitBreaker.state}")
        circuitBreakerStateReportingInterval.foreach(interval =>
          context.system.scheduler.scheduleOnce(interval, self, DisplayCircuitState))
      }
  }

  override def receive: Receive = receiveResolve

  override def preStart() {
    circuitBreaker.defaultActorListeners(self)
      .onWarning((n, max) => log.warning(s"$n failures in a row. $max will cause the circuit to open."))

    context.actorSelection(almhirtContext.localActorPaths.herder) ! almhirt.herder.HerderMessage.RegisterCircuitBreaker(self, circuitBreaker)

    self ! Resolve
  }

  override def postStop() {
    context.actorSelection(almhirtContext.localActorPaths.herder) ! almhirt.herder.HerderMessage.DeregisterCircuitBreaker(self)
  }

} 