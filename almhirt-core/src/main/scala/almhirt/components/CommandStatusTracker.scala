package almhirt.components

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.tracking.{ CommandStatus, CommandStatusChanged, CommandResult }
import almhirt.context.AlmhirtContext
import almhirt.akkax._
import almhirt.akkax.reporting._
import almhirt.akkax.reporting.Implicits._
import akka.stream.actor._
import org.reactivestreams.Subscriber
import akka.stream.scaladsl._

object CommandStatusTracker {
  sealed trait CommandStatusTrackerMessage

  final case class TrackCommand(commandId: CommandId, callback: AlmValidation[CommandResult] ⇒ Unit, deadline: Deadline)

  object TrackCommandMapped {
    def apply(commandId: CommandId, callback: TrackerResult ⇒ Unit, deadline: Deadline): TrackCommand =
      TrackCommand(commandId: CommandId, res ⇒ callback(mapResult(res)), deadline: Deadline)
  }

  sealed trait TrackerResult
  case object TrackedExecutued extends TrackerResult
  final case class TrackedNotExecutued(cause: almhirt.problem.ProblemCause) extends TrackerResult
  case object TrackedTimeout extends TrackerResult
  final case class TrackedError(prob: Problem) extends TrackerResult

  private def mapResult(res: AlmValidation[CommandResult]): TrackerResult =
    res.fold(
      fail ⇒
        fail match {
          case OperationTimedOutProblem(_) ⇒ TrackedTimeout
          case _                           ⇒ TrackedError(fail)
        },
      succ ⇒ succ match {
        case CommandStatus.Executed           ⇒ TrackedExecutued
        case CommandStatus.NotExecuted(cause) ⇒ TrackedNotExecutued(cause)
      })

  def apply(statusTracker: ActorRef): Subscriber[CommandStatusChanged] =
    ActorSubscriber[CommandStatusChanged](statusTracker)

  def propsRaw(targetCacheSize: Int, shrinkCacheAt: Int, checkTimeoutInterval: FiniteDuration, autoConnect: Boolean = false)(implicit ctx: AlmhirtContext): Props = {
    Props(new MyCommandStatusTracker(targetCacheSize, shrinkCacheAt, checkTimeoutInterval, autoConnect))
  }

  def props()(implicit ctx: AlmhirtContext): AlmValidation[Props] = {
    import almhirt.configuration._
    import almhirt.almvalidation.kit._
    for {
      section ← ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.command-status-tracker")
      targetCacheSize ← section.v[Int]("target-cache-size")
      shrinkCacheAt ← section.v[Int]("shrink-cache-at")
      checkTimeoutInterval ← section.v[FiniteDuration]("check-timeout-interval")
      autoConnect ← section.v[Boolean]("auto-connect")
    } yield propsRaw(targetCacheSize, shrinkCacheAt, checkTimeoutInterval, autoConnect)
  }

  val actorname = "command-status-tracker"
  def path(root: RootActorPath) = almhirt.context.ContextActorPaths.misc(root) / actorname
}

private[almhirt] class MyCommandStatusTracker(
  targetCacheSize: Int,
  shrinkCacheAt: Int,
  checkTimeoutInterval: FiniteDuration,
  autoConnect: Boolean)(implicit override val almhirtContext: AlmhirtContext)
    extends AlmActor
    with AlmActorLogging
    with ActorSubscriber
    with ActorLogging
    with ControllableActor
    with StatusReportingActor {
  import CommandStatusTracker._
  import almhirt.storages._

  override val componentControl = LocalComponentControl(self, ActorMessages.ComponentControlActions.none, Some(logWarning))

  implicit def implicitFlowMaterializer = akka.stream.ActorMaterializer()(this.context)

  if (targetCacheSize < 1) throw new Exception(s"targetCacheSize($targetCacheSize) must be grater than zero.")
  if (shrinkCacheAt < targetCacheSize) throw new Exception(s"shrinkCacheAt($targetCacheSize) must at least targetCacheSize($targetCacheSize).")

  logInfo(s"""|target-cache-size: $targetCacheSize
              |shrink-cache-at: $shrinkCacheAt
              |check-timeout-interval: ${checkTimeoutInterval.defaultUnitString}""".stripMargin)

  override val requestStrategy = ZeroRequestStrategy

  implicit val executionContext: ExecutionContext = context.dispatcher

  private case class Entry(callback: AlmValidation[CommandResult] ⇒ Unit, due: Deadline)

  private case object CheckTimeouts
  private case class RemoveEntries(toRemove: Map[CommandId, Set[Long]])

  private[this] var currentId = 0L
  private[this] def nextId: Long = {
    currentId = currentId + 1L
    currentId
  }

  private type EntriesById = Map[Long, Entry]
  private[this] var trackingSubscriptions: Map[CommandId, EntriesById] = Map.empty

  private[this] var cachedStatusLookUp: Map[CommandId, CommandResult] = Map.empty
  private[this] var cachedStatusSeq: Vector[CommandId] = Vector.empty

  private[this] var removedDueToShrinking: Vector[CommandId] = Vector.empty

  private[this] val shrinkSize = (shrinkCacheAt - targetCacheSize) + 1

  private var numTimedOutSubscriptions = 0L
  private var numSubscriptionsRemovedDueToShrinking = 0L
  private var numReceivedStatusChangedEvents = 0L
  private var numReceivedInitiatedStatusChangedEvents = 0L
  private var numReceivedExecutedStatusChangedEvents = 0L
  private var numReceivedNotExecutedStatusChangedEvents = 0L
  private var numReceivedTrackingRequests = 0L
  private var numEffectivelyRemovedSubscriptions = 0L
  private var numSubscriptionsRegularlyNotified = 0L

  private case object IncNumTimedOutSubscriptions
  private case object IncNumSubscriptionsRemovedDueToShrinking
  private case object IncNumEffectivelyRemovedSubscriptions

  private def addStatusToCache(id: CommandId, status: CommandResult) {
    if (cachedStatusSeq.size >= shrinkCacheAt) {
      val (remove, keep) = cachedStatusSeq.splitAt(shrinkSize)
      logDebug(s"Shrunk cache from ${cachedStatusSeq.size} to ${keep.size}.")
      cachedStatusSeq = keep
      cachedStatusLookUp = cachedStatusLookUp -- remove
      removedDueToShrinking ++= remove
    }
    cachedStatusSeq = cachedStatusSeq :+ id
    cachedStatusLookUp = cachedStatusLookUp + (id → status)
  }

  private case object AutoConnect

  def receiveRunning(): Receive = running() {
    reportsStatus(onReportRequested = createStatusReport) {
      case AutoConnect ⇒
        logInfo("Subscribing to event stream.")
        Source(almhirtContext.eventStream).collect { case e: CommandStatusChanged ⇒ e }.to(Sink(CommandStatusTracker(self))).run()
        request(1)

      case TrackCommand(commandId, callback, deadline) ⇒
        numReceivedTrackingRequests = numReceivedTrackingRequests + 1L
        cachedStatusLookUp.get(commandId) match {
          case Some(res) ⇒
            callback(res.success)
            numSubscriptionsRegularlyNotified = numSubscriptionsRegularlyNotified + 1L
          case None ⇒
            trackingSubscriptions.get(commandId) match {
              case Some(entries) ⇒
                trackingSubscriptions = trackingSubscriptions + ((commandId, entries + (nextId → Entry(callback, deadline))))
              case None ⇒
                trackingSubscriptions = trackingSubscriptions + (commandId → Map(nextId → Entry(callback, deadline)))
            }
        }

      case ActorSubscriberMessage.OnNext(next: CommandStatusChanged) ⇒
        numReceivedStatusChangedEvents = numReceivedStatusChangedEvents + 1L

        next.status match {
          case r: CommandResult ⇒
            trackingSubscriptions.get(next.commandHeader.id).foreach { entries ⇒
              r match {
                case CommandStatus.Executed       ⇒ numReceivedExecutedStatusChangedEvents = numReceivedExecutedStatusChangedEvents + 1L
                case CommandStatus.NotExecuted(_) ⇒ numReceivedNotExecutedStatusChangedEvents = numReceivedNotExecutedStatusChangedEvents + 1L
              }
              AlmFuture.compute(entries.foreach(_._2.callback(r.success)))
              numSubscriptionsRegularlyNotified = numSubscriptionsRegularlyNotified + 1L
              trackingSubscriptions = trackingSubscriptions - next.commandHeader.id
            }
            addStatusToCache(next.commandHeader.id, r)
          case CommandStatus.Initiated ⇒ numReceivedInitiatedStatusChangedEvents = numReceivedInitiatedStatusChangedEvents + 1L
        }
        request(1)

      case ActorSubscriberMessage.OnNext(x) ⇒
        log.warning(s"Received unprocessable element $x")
        request(1)

      case CheckTimeouts ⇒
        val currentSubscriptions = trackingSubscriptions
        val currentRemoveDueToShrinking = removedDueToShrinking.toSet

        removedDueToShrinking = Vector.empty

        if (!currentRemoveDueToShrinking.isEmpty) {
          logDebug(s"${currentRemoveDueToShrinking.size} commands will be removed due to shrinking. Subscribers will be notified with a timeout.")
        }

        AlmFuture.compute {
          val deadline = Deadline.now
          val toRemove = currentSubscriptions.map {
            case (id, entries) ⇒
              val timedOutEntries = entries.filter {
                case (entryId, entry) ⇒
                  if (entry.due < deadline) {
                    self ! IncNumTimedOutSubscriptions
                    true
                  } else if (currentRemoveDueToShrinking(id)) {
                    self ! IncNumSubscriptionsRemovedDueToShrinking
                    true
                  } else {
                    false
                  }
              }.map(x ⇒ x._1)
              (id, timedOutEntries.toSet)
          }
          val removeBecauseOfShrinking = currentSubscriptions.map {
            case (id, entries) ⇒
              val timedOutEntries = entries.filter { case (entryId, entry) ⇒ currentRemoveDueToShrinking(id) }.map(x ⇒ x._1)
              (id, timedOutEntries.toSet)
          }
          self ! RemoveEntries(toRemove)
        }

      case RemoveEntries(toRemove) ⇒
        val currentSubscriptions = trackingSubscriptions.toMap

        // Notify timed out
        AlmFuture.compute {
          toRemove.foreach {
            case (commandId, timedOutEntryIds) ⇒
              currentSubscriptions.get(commandId) match {
                case Some(activeSubscriptionsForCommand) ⇒
                  activeSubscriptionsForCommand.view
                    .filter { case (id, entry) ⇒ timedOutEntryIds.contains(id) }
                    .foreach {
                      case (id, entry) ⇒ {
                        self ! IncNumEffectivelyRemovedSubscriptions
                        entry.callback(OperationTimedOutProblem("The tracking timed out. No assumptions can be mede about the progress or result.", Map("tracking-error" -> true, "command-id" -> commandId.value)).failure)
                        reportMinorFailure(OperationTimedOutProblem(s"""Tracking timed out for command id "${commandId.value}"."""))
                      }
                    }
                case None ⇒
                  ()
              }
          }
        }.onFailure { p ⇒
          reportMajorFailure(p)
        }

        //Adjust current subscriptions
        trackingSubscriptions = trackingSubscriptions.map {
          case (commandId, entries) ⇒
            (commandId, entries -- (toRemove.get(commandId).toSeq.flatten))
        }

        //        logDebug(s"""|Stats after removing timed outs:
        //                   |Number of tracked commands: ${trackingSubscriptions.size}
        //                   |Number of subscriptions: ${trackingSubscriptions.values.map { _.size }.sum}
        //                   |To remove due to shrinking: ${removedDueToShrinking.size}(after removal)
        //                   |""".stripMargin)

        context.system.scheduler.scheduleOnce(checkTimeoutInterval, self, CheckTimeouts)

      case IncNumTimedOutSubscriptions ⇒
        numTimedOutSubscriptions = numTimedOutSubscriptions + 1L

      case IncNumSubscriptionsRemovedDueToShrinking ⇒
        numSubscriptionsRemovedDueToShrinking = numSubscriptionsRemovedDueToShrinking + 1L

      case IncNumEffectivelyRemovedSubscriptions ⇒
        numEffectivelyRemovedSubscriptions = numEffectivelyRemovedSubscriptions + 1L
    }

  }

  override def receive: Receive = receiveRunning()

  private def createStatusReport(options: StatusReportOptions): AlmValidation[StatusReport] = {
    val eventDetails = StatusReport().addMany(
      "number-of-received-status-changed-events" -> numReceivedStatusChangedEvents,
      "number-of-received-initiated-status-changed-events" -> numReceivedInitiatedStatusChangedEvents,
      "number-of-received-executed-status-changed-events" -> numReceivedExecutedStatusChangedEvents,
      "number-of-received-not-executed-status-changed-events" -> numReceivedNotExecutedStatusChangedEvents)
    val subscriptionDetails = StatusReport().addMany(
      "number-of-subscriptions" -> trackingSubscriptions.values.map { _.size }.sum,
      "number-of-subscriptions-regularly-notified" -> numSubscriptionsRegularlyNotified,
      "number-of-subscriptions-to-remove-due-to-time-out" -> numTimedOutSubscriptions,
      "number-of-subscriptions-to-remove-due-to-cache-shrinking" -> numSubscriptionsRemovedDueToShrinking,
      "number-of-removed-active-subscriptions" -> numEffectivelyRemovedSubscriptions)

    val rep =
      StatusReport("CommandStatusTracker-Report").withComponentState(componentState) addMany (
        "max-cached-commands" -> shrinkCacheAt,
        "number-of-tracked-commands" -> trackingSubscriptions.size,
        "number-of-received-tracking-requests" -> numReceivedTrackingRequests,
        "subscriptions-details" -> subscriptionDetails,
        "status-changed-events-details" -> eventDetails)
    rep.success
  }

  override def preStart() {
    super.preStart()
    if (autoConnect)
      self ! AutoConnect
    else
      request(1)
    context.system.scheduler.scheduleOnce(checkTimeoutInterval, self, CheckTimeouts)
    registerStatusReporter(description = Some("Stats about tracked commands"))
    registerComponentControl()
    context.parent ! ActorMessages.ConsiderMeForReporting
  }

  override def postStop() {
    super.postStop()
    deregisterComponentControl()
  }
}