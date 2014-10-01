package almhirt.components

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz._, Scalaz._
import scalaz.Validation.FlatMap._
import akka.actor._
import almhirt.common._
import almhirt.tracking.{ CommandStatus, CommandStatusChanged }
import almhirt.context.AlmhirtContext
import akka.stream.actor._
import org.reactivestreams.Subscriber
import akka.stream.scaladsl2._

object CommandStatusTracker {
  sealed trait CommandStatusTrackerMessage

  final case class TrackCommand(commandId: CommandId, callback: AlmValidation[CommandStatus.CommandResult] ⇒ Unit, deadline: Deadline)

  object TrackCommandMapped {
    def apply(commandId: CommandId, callback: TrackerResult ⇒ Unit, deadline: Deadline): TrackCommand =
      TrackCommand(commandId: CommandId, res ⇒ callback(mapResult(res)), deadline: Deadline)
  }

  sealed trait TrackerResult
  case object TrackedExecutued extends TrackerResult
  final case class TrackedNotExecutued(cause: almhirt.problem.ProblemCause) extends TrackerResult
  case object TrackedTimeout extends TrackerResult
  final case class TrackedError(prob: Problem) extends TrackerResult

  private def mapResult(res: AlmValidation[CommandStatus.CommandResult]): TrackerResult =
    res.fold(
      fail ⇒
        fail match {
          case OperationTimedOutProblem(_) ⇒ TrackedTimeout
          case _ ⇒ TrackedError(fail)
        },
      succ ⇒ succ match {
        case CommandStatus.Executed ⇒ TrackedExecutued
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
      section <- ctx.config.v[com.typesafe.config.Config]("almhirt.components.misc.command-status-tracker")
      targetCacheSize <- section.v[Int]("target-cache-size")
      shrinkCacheAt <- section.v[Int]("shrink-cache-at")
      checkTimeoutInterval <- section.v[FiniteDuration]("check-timeout-interval")
      autoConnect <- section.v[Boolean]("auto-connect")
    } yield propsRaw(targetCacheSize, shrinkCacheAt, checkTimeoutInterval, autoConnect)
  }

  val actorname = "command-status-tracker"
}

private[almhirt] class MyCommandStatusTracker(
  targetCacheSize: Int,
  shrinkCacheAt: Int,
  checkTimeoutInterval: FiniteDuration,
  autoConnect: Boolean)(implicit ctx: AlmhirtContext)
  extends ActorSubscriber
  with ActorLogging
  with ImplicitFlowMaterializer {
  import CommandStatusTracker._
  import almhirt.storages._

  if (targetCacheSize < 1) throw new Exception(s"targetCacheSize($targetCacheSize) must be grater than zero.")
  if (shrinkCacheAt < targetCacheSize) throw new Exception(s"shrinkCacheAt($targetCacheSize) must at least targetCacheSize($targetCacheSize).")

  override val requestStrategy = ZeroRequestStrategy

  implicit val executionContext: ExecutionContext = context.dispatcher

  private case class Entry(callback: AlmValidation[CommandStatus.CommandResult] ⇒ Unit, due: Deadline)

  private case object CheckTimeouts
  private case class RemoveTimedOut(timedOut: Map[CommandId, Set[Long]])

  private[this] var currentId = 0L
  private[this] def nextId: Long = {
    currentId = currentId + 1L
    currentId
  }

  private type EntriesById = Map[Long, Entry]
  private[this] var trackingSubscriptions: Map[CommandId, EntriesById] = Map.empty

  private[this] var cachedStatusLookUp: Map[CommandId, CommandStatus.CommandResult] = Map.empty
  private[this] var cachedStatusSeq: Vector[CommandId] = Vector.empty

  private[this] val shrinkSize = (shrinkCacheAt - targetCacheSize) + 1

  private def addStatusToCache(id: CommandId, status: CommandStatus.CommandResult) {
    if (cachedStatusSeq.size == shrinkCacheAt) {
      val (remove, keep) = cachedStatusSeq.splitAt(shrinkSize)
      cachedStatusSeq = keep
      cachedStatusLookUp = cachedStatusLookUp -- remove
    }
    cachedStatusSeq = cachedStatusSeq :+ id
    cachedStatusLookUp = cachedStatusLookUp + (id -> status)
  }

  private case object AutoConnect

  def running(): Receive = {
    case AutoConnect ⇒
      log.info("Subscribing to event stream.")
      FlowFrom(ctx.eventStream).collect { case e: CommandStatusChanged ⇒ e }.publishTo(CommandStatusTracker(self))
      request(1)

    case TrackCommand(commandId, callback, deadline) ⇒
      cachedStatusLookUp.get(commandId) match {
        case Some(res) ⇒
          callback(res.success)
        case None ⇒
          trackingSubscriptions.get(commandId) match {
            case Some(entries) ⇒
              trackingSubscriptions = trackingSubscriptions + ((commandId, entries + (nextId -> Entry(callback, deadline))))
            case None ⇒
              trackingSubscriptions = trackingSubscriptions + (commandId -> Map(nextId -> Entry(callback, deadline)))
          }
      }

    case ActorSubscriberMessage.OnNext(next: CommandStatusChanged) ⇒
      next.status match {
        case r: CommandStatus.CommandResult ⇒
          trackingSubscriptions.get(next.commandHeader.id).foreach { entries ⇒
            AlmFuture.compute(entries.foreach(_._2.callback(r.success)))
            trackingSubscriptions = trackingSubscriptions - next.commandHeader.id
          }
          addStatusToCache(next.commandHeader.id, r)
        case _ ⇒
          ()
      }
      request(1)

    case ActorSubscriberMessage.OnNext(x) ⇒
      log.warning(s"Received unprocessable element $x")
      request(1)

    case CheckTimeouts ⇒
      val currentSubscriptions = trackingSubscriptions
      AlmFuture.compute {
        val deadline = Deadline.now
        val timedOut = currentSubscriptions.map {
          case (id, entries) ⇒
            val timedOutEntries = entries.filter { case (entryId, entry) ⇒ entry.due < deadline }.map(x ⇒ x._1)
            (id, timedOutEntries.toSet)
        }
        self ! RemoveTimedOut(timedOut)
      }

    case RemoveTimedOut(timedOut) ⇒
      val currentSubscriptions = trackingSubscriptions
      AlmFuture.compute {
        timedOut.foreach {
          case (commandId, timedOutEntryIds) ⇒
            val activeSubscriptionsForCommand = currentSubscriptions.get(commandId) | Map.empty
            activeSubscriptionsForCommand.view
              .filter { case (id, entry) ⇒ timedOutEntryIds.contains(id) }
              .foreach { case (id, entry) ⇒ entry.callback(OperationTimedOutProblem("The tracking timed out.").failure) }
        }
      }
      trackingSubscriptions = trackingSubscriptions.map {
        case (commandId, entries) ⇒
          (commandId, entries -- (timedOut.get(commandId).toSeq.flatten))
      }
      context.system.scheduler.scheduleOnce(checkTimeoutInterval, self, CheckTimeouts)

  }

  override def receive: Receive = running()

  override def preStart() {
    super.preStart()
    if (autoConnect)
      self ! AutoConnect
    else
      request(1)
    context.system.scheduler.scheduleOnce(checkTimeoutInterval, self, CheckTimeouts)
  }
}