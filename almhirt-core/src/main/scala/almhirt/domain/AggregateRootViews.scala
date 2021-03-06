package almhirt.domain

import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.concurrent.duration._
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.aggregates._
import almhirt.almvalidation.kit._
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import almhirt.context.AlmhirtContext
import akka.stream.actor._
import org.reactivestreams.Publisher
import akka.stream.scaladsl._
import akka.stream.Materializer

object AggregateRootViews {
  type ViewConstructor = (AggregateRootId, ActorRef, Option[ActorRef]) ⇒ AlmhirtContext ⇒ Props

  def propsRaw[E <: AggregateRootEvent: ClassTag](
    viewConstructor: ViewConstructor,
    aggregateEventLogToResolve: ToResolve,
    snapShotStorageToResolve: Option[ToResolve],
    resolveSettings: ResolveSettings,
    eventBufferSize: Int,
    connectTo: Option[Publisher[Event]])(implicit ctx: AlmhirtContext): Props =
    Props(new AggregateRootViews[E](viewConstructor, aggregateEventLogToResolve, snapShotStorageToResolve, resolveSettings, eventBufferSize, connectTo))

  def props[E <: AggregateRootEvent: ClassTag](
    viewConstructorFromConf: com.typesafe.config.Config ⇒ AlmValidation[ViewConstructor],
    viewsConfigName: Option[String] = None)(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    val aggregateEventLogToResolve = ResolvePath(ctx.localActorPaths.eventLogs / almhirt.eventlog.AggregateRootEventLog.actorname)
    val snapshotRepositoryToResolve = ResolvePath(ctx.localActorPaths.misc / almhirt.snapshots.SnapshotRepository.actorname)
    val path = "almhirt.components.views." + viewsConfigName.getOrElse("default-views")
    for {
      section ← ctx.config.v[com.typesafe.config.Config](path)
      viewSection ← section.v[com.typesafe.config.Config]("view")
      viewConstructor ← viewConstructorFromConf(viewSection)
      snapShotStorageToResolve ← section.v[Boolean]("use-snapshots").map {
        case true  ⇒ Some(snapshotRepositoryToResolve)
        case false ⇒ None
      }
      resolveSettings ← section.v[ResolveSettings]("resolve-settings")
      eventBufferSize ← section.v[Int]("event-buffer-size")
    } yield propsRaw(
      viewConstructor,
      aggregateEventLogToResolve,
      snapShotStorageToResolve,
      resolveSettings,
      eventBufferSize,
      Some(ctx.eventStream))
  }

  def subscribeTo[E <: Event](
    publisher: Publisher[Event],
    views: ActorRef)(implicit mat: Materializer, tag: scala.reflect.ClassTag[E]) {
    Source.fromPublisher(publisher).filter(p ⇒ tag.runtimeClass.isInstance(p)).map(_.asInstanceOf[E]).to(Sink.fromSubscriber(ActorSubscriber[E](views))).run()
  }
}

private[almhirt] object AggregateRootViewsInternals {
  import almhirt.problem.ProblemCause
  final case class ReportViewDebug(msg: String)
  final case class ReportViewError(msg: String, cause: ProblemCause)
  final case class ReportViewWarning(msg: String, cause: ProblemCause)
  final case class CargoJettisoned(aggId: AggregateRootId)
}

/**
 * Manages views on an aggregate root, where there is one actor view on a single aggregate root.
 *  The views receive an [[AggregateRootViewMessages.ApplyAggregateRootEvent]] message on an aggregate event for the aggregate root and must deliver their
 *  view when receiving [[AggregateRootViewMessages.GetAggregateRootProjection]].
 *
 *  On [[AggregateRootViewMessages.ApplyAggregateRootEvent]] an [[AggregateRootViewMessages.AggregateRootEventHandled]] is expected.
 *  On [[AggregateRootViewMessages.GetAggregateRootProjection]] the view must send a custom result to the sender of [[GetAggregateRootProjection]].
 *
 *  The Actor is an ActorSubscriber that requests [[AggregateRootEvents]].
 */
class AggregateRootViews[E <: AggregateRootEvent](
  override val viewConstructor: AggregateRootViews.ViewConstructor,
  override val aggregateEventLogToResolve: ToResolve,
  override val snapShotStorageToResolve: Option[ToResolve],
  override val resolveSettings: ResolveSettings,
  override val eventBufferSize: Int,
  override val connectTo: Option[Publisher[Event]] = None)(implicit override val eventTag: scala.reflect.ClassTag[E], override val almhirtContext: AlmhirtContext) extends AggregateRootViewsSkeleton[E]

private[almhirt] trait AggregateRootViewsSkeleton[E <: AggregateRootEvent] extends AlmActor with AlmActorLogging with ActorSubscriber with ActorLogging with ControllableActor with StatusReportingActor {
  import AggregateRootViewMessages._

  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)

  implicit private val executor = almhirtContext.futuresContext

  override val componentControl = LocalComponentControl(self, ComponentControlActions.none, Some(logWarning))

  private var numJettisonedSinceLastReport = 0L
  private var numReportedViewErrors = 0L
  private var numReportedViewWarnings = 0L

  private var numEscalatedViewErrors = 0L
  private var firstEscalatedViewErrorOn: Option[java.time.LocalDateTime] = None
  private var lastEscalatedViewErrorOn: Option[java.time.LocalDateTime] = None

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 20, withinTimeRange = 1 minute) {
      case exn: Exception ⇒
        numEscalatedViewErrors = numEscalatedViewErrors + 1L
        lastEscalatedViewErrorOn = Some(almhirtContext.getUtcTimestamp)
        if (firstEscalatedViewErrorOn.isEmpty)
          firstEscalatedViewErrorOn = Some(almhirtContext.getUtcTimestamp)
        informVeryImportant(s"""Handling escalated error of type ${exn.getClass.getName}("${exn.getMessage}") for ${sender.path.name} with action Restart.""")
        Restart
    }

  def viewConstructor: AggregateRootViews.ViewConstructor

  def aggregateEventLogToResolve: ToResolve
  def snapShotStorageToResolve: Option[ToResolve]
  def resolveSettings: ResolveSettings

  def connectTo: Option[Publisher[Event]]

  implicit def eventTag: scala.reflect.ClassTag[E]
  def eventBufferSize: Int

  final override val requestStrategy = ZeroRequestStrategy

  private case object Resolve

  def receiveResolve: Receive = startup() {
    reportsStatus(onReportRequested = createStatusReport) {
      case Resolve ⇒
        val actorsToResolve =
          Map("aggregateeventlog" → aggregateEventLogToResolve) ++
            snapShotStorageToResolve.map(r ⇒ Map("snapshotstorage" → r)).getOrElse(Map.empty)
        context.resolveMany(actorsToResolve, resolveSettings, None, Some("resolver"))

      case ActorMessages.ManyResolved(dependencies, _) ⇒
        logInfo("Found dependencies.")
        connectTo match {
          case Some(publisher) ⇒
            logInfo("Subscribing myself.")
            AggregateRootViews.subscribeTo[E](publisher, self)
          case None ⇒
            ()
        }
        request(eventBufferSize)
        context.become(receiveRunning(dependencies("aggregateeventlog"), dependencies.get("snapshotstorage")))
        logInfo("Running..")

      case ActorMessages.ManyNotResolved(problem, _) ⇒
        logError(s"Failed to resolve dependencies:\n$problem")
        sys.error(s"Failed to resolve dependencies.")
    }
  }

  private def receiveRunning(aggregateEventLog: ActorRef, snapshotStorage: Option[ActorRef]): Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case GetAggregateRootProjectionFor(id) ⇒
        val view = context.child(id.value) match {
          case Some(v) ⇒
            v
          case None ⇒
            val props = viewConstructor(id, aggregateEventLog, snapshotStorage)(this.almhirtContext)
            val actor = context.actorOf(props, id.value)
            //context watch actor
            actor
        }
        view forward GetAggregateRootProjection

      case ActorSubscriberMessage.OnNext(event: AggregateRootEvent) ⇒
        event.castTo[E].fold(
          fail ⇒ {
            // This can happen quite often depending on the producer ...
            logWarning(s"Received unproccessable aggregate event:\n$fail")
            request(1)
          },
          aggregateEvent ⇒ {
            context.child(aggregateEvent.aggId.value) match {
              case Some(v) ⇒
                v ! ApplyAggregateRootEvent(aggregateEvent)
              case None ⇒
                request(1)
            }
          })

      case AggregateRootEventHandled ⇒
        request(1)

      case ActorSubscriberMessage.OnNext(x) ⇒
        logWarning(s"""Received unproccessable message from publisher: ${x.getClass.getName()}".""")
        request(1)

      case ActorSubscriberMessage.OnError(ex) ⇒
        throw new Exception(s"""I("${self.path}") received an error via the stream.""", ex)

      case ActorSubscriberMessage.OnComplete ⇒
        context.stop(self)

      case AggregateRootViewsInternals.ReportViewDebug(msg) ⇒
        logDebug(s"View ${sender().path.name} reported a debug message: $msg")

      case AggregateRootViewsInternals.ReportViewError(msg, cause) ⇒
        numReportedViewErrors = numReportedViewErrors + 1L
        logError(s"View ${sender().path.name} reported an error: $msg")
        reportMajorFailure(cause)

      case AggregateRootViewsInternals.ReportViewWarning(msg, cause) ⇒
        numReportedViewWarnings = numReportedViewWarnings + 1L
        logWarning(s"View ${sender().path.name} reported a warning: $msg")
        reportMinorFailure(cause)

      case AggregateRootHiveInternals.CargoJettisoned(id) ⇒
        this.numJettisonedSinceLastReport += 1L
    }
  }

  def receive: Receive = receiveResolve

  def createStatusReport(options: StatusReportOptions): AlmValidation[StatusReport] = {
    this.numJettisonedSinceLastReport = 0
    val numberOfViews = this.context.children.size

    val rep = StatusReport(s"${this.getClass.getSimpleName}-Report") addMany
      ("number-of-views" -> numberOfViews) subReport ("errors",
        "number-of-escalated-view-errors" -> numEscalatedViewErrors,
        "first-escalated-view-error-on" -> firstEscalatedViewErrorOn,
        "last-escalated-view-error-on" -> lastEscalatedViewErrorOn) subReport ("views",
          "number-of-reported-view-warnings" -> numReportedViewWarnings,
          "number-of-reported-view-errors" -> numReportedViewErrors,
          "number-of-views-jettisoned-since-last-report" -> numJettisonedSinceLastReport)

    rep.success
  }

  override def preStart() {
    logInfo("Starting...")
    logInfo(s"SnapshotStorage: ${snapShotStorageToResolve}")
    logInfo(s"event buffer size: ${eventBufferSize}")
    registerComponentControl()
    registerStatusReporter(description = None)
    context.parent ! ActorMessages.ConsiderMeForReporting
    self ! Resolve
  }

  override def postStop() {
    super.postStop()
    deregisterComponentControl()
    deregisterStatusReporter()
  }

}