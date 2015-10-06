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
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
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
    eventlogCallTimeout: FiniteDuration,
    storeEventRetrySettings: RetryPolicyExt,
    autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props = {
    Props(new EventLogWriterImpl(
      eventLogToResolve,
      resolveSettings,
      warningThreshold,
      autoConnect,
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
          storeEventRetrySettings ← section.v[RetryPolicyExt]("store-event-retry-settings")
          eventlogCallTimeout ← section.v[FiniteDuration]("event-log-call-timeout")
        } yield propsRaw(ResolvePath(eventlogPath), resolveSettings, warningThreshold, eventlogCallTimeout, storeEventRetrySettings, autoConnect)
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
    eventlogCallTimeout: FiniteDuration,
    storeEventRetrySettings: RetryPolicyExt)(implicit override val almhirtContext: AlmhirtContext) extends ActorSubscriber with AlmActor with AlmActorLogging with ActorLogging with ControllableActor with StatusReportingActor {
  import almhirt.eventlog.EventLog

  override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)
  implicit val executor = almhirtContext.futuresContext

  override val requestStrategy = ZeroRequestStrategy

  private case object AutoConnect
  private case object Resolve

  private var loggedEvents = 0L
  private var eventsNotLogged = 0L
  private var eventsReceived = 0L

  private var eventLog: ActorRef = null

  def receiveResolve: Receive = startup() {
    reportsStatusF(onReportRequested = createStatusReport) {
      case Resolve ⇒
        context.resolveSingle(eventLogToResolve, resolveSettings, None, Some("event-log-resolver"))

      case ActorMessages.ResolvedSingle(resolvedEventlog, _) ⇒
        logInfo("Found event log.")
        this.eventLog = resolvedEventlog
        if (autoConnect)
          self ! AutoConnect
        else
          request(1)
        context.become(receiveRunning)

      case ActorMessages.SingleNotResolved(problem, _) ⇒
        logError(s"Could not resolve event log @${eventLogToResolve}:\n$problem")
        sys.error(s"Could not resolve event log @${eventLogToResolve}.")
        reportCriticalFailure(problem)
    }
  }

  def receiveRunning: Receive = running() {
    reportsStatusF(onReportRequested = createStatusReport) {
      case AutoConnect ⇒
        logInfo("Subscribing to event stream.")
        Source(almhirtContext.eventStream).to(Sink(EventLogWriter(self))).run()
        request(1)

      case ActorSubscriberMessage.OnNext(event: Event) ⇒
        eventsReceived = eventsReceived + 1L
        if (!event.header.noLoggingSuggested) {
          val start = Deadline.now
          val f = {
            this.retryFuture(storeEventRetrySettings) {
              (eventLog ? EventLog.LogEvent(event, true))(eventlogCallTimeout).mapCastTo[EventLog.LogEventResponse].foldV(
                fail ⇒ {
                  fail match {
                    case AlreadyExistsProblem(p) ⇒
                      logWarning(s"Event event ${event.eventId} already existed. This can happen when a write operation timed out but the event was stored afterwards by the storage.")
                      EventLog.EventLogged(event.eventId).success
                    case _ ⇒ {
                      fail.failure
                    }
                  }
                },
                succ ⇒ succ.success)
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
        loggedEvents = loggedEvents + 1L
        request(1)

      case EventLog.EventNotLogged(id, problem) ⇒
        eventsNotLogged = eventsNotLogged + 1L
        logError(s"Could not log event '${id.value}':\n$problem")
        reportMajorFailure(problem)
        request(1)
    }
  }

  override def receive: Receive = receiveResolve

  def createStatusReport(options: StatusReportOptions): AlmFuture[StatusReport] = {
    val baseReport = StatusReport("EventLogWriter-Report").withComponentState(componentState) addMany
      ("events-received" -> eventsReceived,
        "events-logged" -> loggedEvents,
        "events-not-logged" -> eventsNotLogged,
        "event-log" -> eventLog.path.toStringWithoutAddress)

    AlmFuture.successful(baseReport)
  }

  override def preStart() {
    super.preStart()
    registerComponentControl()
    registerStatusReporter(description = Some("Simply writes to an event log."))
    context.parent ! ActorMessages.ConsiderMeForReporting
    self ! Resolve
  }

  override def postStop() {
    super.postStop()
    deregisterComponentControl()
    deregisterStatusReporter()
  }
} 