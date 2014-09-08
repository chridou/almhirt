package almhirt.components

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.common._
import almhirt.tracking.{ CommandStatus, CommandStatusChanged }

import akka.stream.actor._
import org.reactivestreams.Subscriber
import akka.stream.scaladsl.Duct
import akka.stream.FlowMaterializer

object CommandStatusTracker {
  sealed trait CommandStatusTrackerMessage

  final case class TrackCommand(commandId: CommandId, callback: AlmValidation[CommandStatus.CommandResult] => Unit, deadline: Deadline)

  def apply(statusTracker: ActorRef): Subscriber[CommandStatusChanged] =
    ActorSubscriber[CommandStatusChanged](statusTracker)

  def systemEventSubscriber(statusTracker: ActorRef)(implicit materializer: FlowMaterializer): Subscriber[SystemEvent] = {
    val duct = Duct[SystemEvent].collect { case e: CommandStatusChanged => e }
    val (subscriber, publisher) = duct.build
    val trackingSubscriber = CommandStatusTracker(statusTracker)
    publisher.subscribe(trackingSubscriber)
    subscriber
  }

  def props(targetCacheSize: Int, shrinkCacheAt: Int, checkTimeoutInterval: FiniteDuration): Props = {
    Props(new MyCommandStatusTracker(targetCacheSize, shrinkCacheAt, checkTimeoutInterval))
  }
}

private[almhirt] class MyCommandStatusTracker(targetCacheSize: Int, shrinkCacheAt: Int, checkTimeoutInterval: FiniteDuration) extends ActorSubscriber with ActorLogging {
  import CommandStatusTracker._
  import almhirt.storages._
  override val requestStrategy = ZeroRequestStrategy

  implicit val executionContext: ExecutionContext = context.dispatcher

  private case class Entry(callback: AlmValidation[CommandStatus.CommandResult] => Unit, due: Deadline)

  private case object CheckTimeouts
  private case class RemoveTimedOut(timedOut: Map[CommandId, Set[Long]])

  private[this] var currentId = 0L
  private def nextId: Long = {
    currentId = currentId + 1L
    currentId
  }

  private type EntriesById = Map[Long, Entry]
  private[this] var trackingSubscriptions: Map[CommandId, EntriesById] = Map.empty

  private[this] var cachedStatusLookUp: Map[CommandId, CommandStatus.CommandResult] = Map.empty
  private[this] var cachedStatusSeq: Vector[CommandId] = Vector.empty

  private[this] val shrinkSize = shrinkCacheAt - targetCacheSize

  private def addStatusToCache(id: CommandId, status: CommandStatus.CommandResult) {
    if (cachedStatusSeq.size == shrinkCacheAt) {
      val (remove, keep) = cachedStatusSeq.splitAt(shrinkSize)
      cachedStatusSeq = keep
      cachedStatusLookUp = cachedStatusLookUp -- remove
    }
    cachedStatusSeq = cachedStatusSeq :+ id
    cachedStatusLookUp = cachedStatusLookUp + (id -> status)
  }

  def running(): Receive = {
    case TrackCommand(commandId, callback, deadline) =>
      cachedStatusLookUp.get(commandId) match {
        case Some(res) =>
          callback(res.success)
        case None =>
          trackingSubscriptions.get(commandId) match {
            case Some(entries) =>
              trackingSubscriptions = trackingSubscriptions + ((commandId, entries + (nextId -> Entry(callback, deadline))))
            case None =>
              trackingSubscriptions = trackingSubscriptions + (commandId -> Map(nextId -> Entry(callback, deadline)))
          }
      }

    case ActorSubscriberMessage.OnNext(next: CommandStatusChanged) =>
      next.status match {
        case r: CommandStatus.CommandResult =>
          trackingSubscriptions.get(next.commandHeader.id).foreach { entries =>
            AlmFuture.compute(entries.foreach(_._2.callback(r.success)))
            trackingSubscriptions = trackingSubscriptions - next.commandHeader.id
            addStatusToCache(next.commandHeader.id, r)
          }
        case _ =>
          ()
      }
      request(1)

    case ActorSubscriberMessage.OnNext(x) =>
      log.warning(s"Received unprocessable element $x")
      request(1)

    case CheckTimeouts =>
      val currentSubscriptions = trackingSubscriptions
      AlmFuture.compute {
        val deadline = Deadline.now
        val timedOut = currentSubscriptions.map {
          case (id, entries) =>
            val timedOutEntries = entries.filter { case (entryId, entry) => entry.due < deadline }.map(x => x._1)
            (id, timedOutEntries.toSet)
        }
        self ! RemoveTimedOut(timedOut)
      }

    case RemoveTimedOut(timedOut) =>
      val currentSubscriptions = trackingSubscriptions
      AlmFuture.compute {
        timedOut.foreach {
          case (commandId, timedOutEntryIds) =>
            val activeSubscriptionsForCommand = currentSubscriptions.get(commandId) | Map.empty
            activeSubscriptionsForCommand.view
              .filter { case (id, entry) => timedOutEntryIds.contains(id) }
              .foreach { case (id, entry) => entry.callback(OperationTimedOutProblem("The tracking timed out.").failure) }
        }
      }
      trackingSubscriptions = trackingSubscriptions.map {
        case (commandId, entries) =>
          (commandId, entries -- (timedOut.get(commandId).toSeq.flatten))
      }
      context.system.scheduler.scheduleOnce(checkTimeoutInterval, self, CheckTimeouts)

  }

  override def receive: Receive = running()

  override def preStart() {
    super.preStart()
    request(1)
    context.system.scheduler.scheduleOnce(checkTimeoutInterval, self, CheckTimeouts)
  }
}