package almhirt.components.impl

import org.joda.time.LocalDateTime
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.components.ExecutionStateTracker
import almhirt.commanding._
import almhirt.messaging.MessagePublisher
import almhirt.problem.{ Major, Minor }

object ExecutionTrackerTemplate {
  final case class TrackingEntry(currentState: ExecutionState, lastModified: LocalDateTime) {
    def isFinished: Boolean = currentState match {
      case _: ExecutionFinishedState => true
      case _ => false
    }

    def tryGetFinished: Option[ExecutionFinishedState] =
      currentState match {
        case f: ExecutionFinishedState => Some(f)
        case _ => None
      }
  }

  object TrackingEntry {
    def apply(currentState: ExecutionState)(implicit ccuad: CanCreateDateTime): TrackingEntry =
      TrackingEntry(currentState, ccuad.getUtcTimestamp)
  }

  trait SecondLevelStoreWrapper {
    def store(entry: TrackingEntry)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Unit]
    def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[TrackingEntry]]
    def getAllYoungerThan(age: org.joda.time.Duration)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Seq[TrackingEntry]]
    def removeAllOlderThan(age: org.joda.time.Duration)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Unit]
  }

  object SecondLevelStoreWrapper {
    sealed trait SecondLevelStoreWrapperMessage
    sealed trait StoreEntryResponse
    final case class StoreEntry(entry: TrackingEntry) extends SecondLevelStoreWrapperMessage
    final case class StoreEntryState(problem: Option[Problem]) extends StoreEntryResponse
    sealed trait GetEntryResponse extends SecondLevelStoreWrapperMessage
    final case class GetEntry(trackId: String) extends SecondLevelStoreWrapperMessage
    final case class GetEntryResult(entry: Option[TrackingEntry]) extends GetEntryResponse
    final case class GetEntryFailure(problem: Problem) extends GetEntryResponse
    sealed trait GetAllYoungerThanResponse extends SecondLevelStoreWrapperMessage
    final case class GetAllYoungerThan(age: org.joda.time.Duration) extends SecondLevelStoreWrapperMessage
    final case class GetAllYoungerThanResult(entries: Seq[TrackingEntry]) extends GetAllYoungerThanResponse
    final case class GetAllYoungerThanFailure(problem: Problem) extends GetAllYoungerThanResponse
    sealed trait RemoveAllOlderThanResponse extends SecondLevelStoreWrapperMessage
    final case class RemoveAllOlderThan(age: org.joda.time.Duration) extends SecondLevelStoreWrapperMessage
    final case class RemoveAllOlderThanState(problem: Option[Problem]) extends RemoveAllOlderThanResponse

    import scalaz.syntax.validation._
    import akka.pattern.ask
    import akka.util.Timeout
    import almhirt.almfuture.all._

    def apply(actor: ActorRef)(implicit executionContext: ExecutionContext): SecondLevelStoreWrapper = {
      new SecondLevelStoreWrapper {
        def store(entry: TrackingEntry)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Unit] =
          (actor ? StoreEntry(entry))(atMost).successfulAlmFuture[StoreEntryResponse].mapV(res =>
            res match {
              case StoreEntryState(None) => ().success
              case StoreEntryState(Some(problem)) => problem.failure
            })

        def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[TrackingEntry]] =
          (actor ? GetEntry(trackId))(atMost).successfulAlmFuture[GetEntryResponse].mapV(res =>
            res match {
              case GetEntryResult(entry) => entry.success
              case GetEntryFailure(problem) => problem.failure
            })

        def getAllYoungerThan(age: org.joda.time.Duration)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Seq[TrackingEntry]] =
          (actor ? GetAllYoungerThan(age))(atMost).successfulAlmFuture[GetAllYoungerThanResponse].mapV(res =>
            res match {
              case GetAllYoungerThanResult(entries) => entries.success
              case GetAllYoungerThanFailure(problem) => problem.failure
            })

        def removeAllOlderThan(age: org.joda.time.Duration)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Unit] =
          (actor ? RemoveAllOlderThan(age))(atMost).successfulAlmFuture[RemoveAllOlderThanResponse].mapV(res =>
            res match {
              case RemoveAllOlderThanState(None) => ().success
              case RemoveAllOlderThanState(Some(problem)) => problem.failure
            })
      }
    }
  }
}

trait ExecutionTrackerTemplate { actor: ExecutionStateTracker with Actor with ActorLogging =>
  import ExecutionStateTracker._
  import ExecutionTrackerTemplate._

  implicit def publishTo: MessagePublisher
  implicit def canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes
  implicit def executionContext: ExecutionContext
  def secondLevelStore: SecondLevelStoreWrapper
  def secondLevelMaxAskDuration: scala.concurrent.duration.FiniteDuration

  private case class StateUpdate(newState: Map[String, TrackingEntry])

  protected def waitingForTrackedStateUpdate(tracked: Map[String, TrackingEntry], subscriptions: Map[String, List[ActorRef]], updateRequests: Vector[ExecutionState]): Receive = {
    case StateUpdate(newState) =>
      val newSubscriptions = notifyFinishedStateSubscribers(tracked, subscriptions)
      if (updateRequests.isEmpty)
        context.become(idleState(tracked, newSubscriptions))
      else {
        handleIncomingExecutionState(updateRequests.head, tracked)
        context.become(waitingForTrackedStateUpdate(tracked, subscriptions, updateRequests.tail))
      }
    case st: ExecutionState =>
      context.become(waitingForTrackedStateUpdate(tracked, subscriptions, updateRequests :+ st))
    case GetExecutionStateFor(trackId) =>
      reportExecutionState(trackId, tracked, sender)
    case SubscribeForFinishedState(trackId, subscribeMe) =>
      preSubscribe(trackId, subscribeMe, tracked) match {
        case Some(subscriber) => context.become(waitingForTrackedStateUpdate(tracked, addSubscription(trackId, subscriber, subscriptions), updateRequests))
        case None => ()
      }
    case UnsubscribeForFinishedState(trackId, unsubscribeMe) =>
      context.become(waitingForTrackedStateUpdate(tracked, removeSubscription(trackId, unsubscribeMe, subscriptions), updateRequests))
    case RemoveOldExecutionStates(maxAge) =>
      val (newTracked, newSubscriptions) = cleanUp(maxAge, tracked, subscriptions)
      context.become(waitingForTrackedStateUpdate(newTracked, newSubscriptions, updateRequests))
  }

  protected def idleState(tracked: Map[String, TrackingEntry], subscriptions: Map[String, List[ActorRef]]): Receive = {
    case st: ExecutionState =>
      handleIncomingExecutionState(st, tracked)
      context.become(waitingForTrackedStateUpdate(tracked, subscriptions, Vector.empty))
    case GetExecutionStateFor(trackId) =>
      reportExecutionState(trackId, tracked, sender)
    case SubscribeForFinishedState(trackId, subscribeMe) =>
      preSubscribe(trackId, subscribeMe, tracked) match {
        case Some(subscriber) => context.become(idleState(tracked, addSubscription(trackId, subscriber, subscriptions)))
        case None => ()
      }
    case UnsubscribeForFinishedState(trackId, unsubscribeMe) =>
      context.become(idleState(tracked, removeSubscription(trackId, unsubscribeMe, subscriptions)))
    case RemoveOldExecutionStates(maxAge) =>
      val (newTracked, newSubscriptions) = cleanUp(maxAge, tracked, subscriptions)
      context.become(idleState(newTracked, newSubscriptions))
  }

  def handleIncomingExecutionState(incomingState: ExecutionState, tracked: Map[String, TrackingEntry]) {
    getUpdatedState(incomingState, tracked).fold(
      fail => publishTo.publish(FailureEvent(s"""Could not determine the state to update for tracking id "${incomingState.trackId}"""", fail, Minor)),
      potUpdatedState => {
        potUpdatedState match {
          case Some(updatedState) =>
            storeEntryToSecondLevelStore(updatedState)
            self ! StateUpdate(tracked + (updatedState.currentState.trackId -> updatedState))
          case None =>
            self ! StateUpdate(tracked)
        }
      })
  }

  private def getUpdatedState(incomingState: ExecutionState, tracked: Map[String, TrackingEntry]): AlmFuture[Option[TrackingEntry]] = {
    getExecutionState(incomingState.trackId, tracked).map {
      case None =>
        Some(TrackingEntry(incomingState))
      case Some(oldState) =>
        if (ExecutionState.compareExecutionState(oldState.currentState, incomingState) > 0)
          Some(TrackingEntry(incomingState))
        else
          None
    }
  }

  private def storeEntryToSecondLevelStore(entry: TrackingEntry) {
    secondLevelStore.store(entry)(secondLevelMaxAskDuration).onFailure { prob =>
      log.error(s"""The second level store did not store the tracking entry with tarck id "${entry.currentState.trackId}". The error message was "${prob.message}""""")
      publishTo.publish(FailureEvent(s"""The second level store did not store the tracking entry with tarck id "${entry.currentState.trackId}"""", prob, Minor))
    }
  }

  private def getExecutionState(trackId: String, tracked: Map[String, TrackingEntry]): AlmFuture[Option[TrackingEntry]] =
    (tracked.get(trackId) match {
      case Some(entry) => AlmFuture.successful(Some(entry))
      case None => secondLevelStore.get(trackId)(secondLevelMaxAskDuration)
    })

  private def reportExecutionState(trackId: String, tracked: Map[String, TrackingEntry], respondTo: ActorRef): Unit = {
    val pinnedSender = respondTo
    getExecutionState(trackId, tracked).onComplete(
      fail => {
        pinnedSender ! CurrentExecutionState(trackId, None)
        publishTo.publish(FailureEvent(s"""Could not get the execution state for tracking id "$trackId".""", fail, Major))
      },
      succ => sender ! CurrentExecutionState(trackId, succ.map(_.currentState)))
  }

  private def preSubscribe(trackId: String, subscriber: ActorRef, tracked: Map[String, TrackingEntry]): Option[ActorRef] = {
    getFinishedStates(tracked).get(trackId) match {
      case None =>
        Some(subscriber)
      case Some(finishedState) =>
        subscriber ! FinishedExecutionStateResult(finishedState)
        None
    }
  }

  private def notifyFinishedStateSubscribers(tracked: Map[String, TrackingEntry], subscriptions: Map[String, List[ActorRef]]): Map[String, List[ActorRef]] = {
    getFinishedStates(tracked).foldLeft(subscriptions) { (acc, cur) =>
      acc.get(cur._1) match {
        case Some(subscribers) =>
          subscribers foreach (_ ! FinishedExecutionStateResult(cur._2))
          acc - cur._1
        case None => acc
      }
    }
  }

  private def addSubscription(trackId: String, subscriber: ActorRef, subscriptions: Map[String, List[ActorRef]]): Map[String, List[ActorRef]] =
    subscriptions.get(trackId) match {
      case None => subscriptions + (trackId -> (subscriber :: Nil))
      case Some(subscribers) =>
        if (subscribers.exists(_ == subscriber))
          subscriptions
        else
          subscriptions + (trackId -> (subscriber :: subscribers))
    }

  private def removeSubscription(trackId: String, subscriber: ActorRef, subscriptions: Map[String, List[ActorRef]]): Map[String, List[ActorRef]] =
    subscriptions get (trackId) match {
      case None => subscriptions
      case Some(subscribers) =>
        subscribers filterNot (_ == subscriber) match {
          case Nil => subscriptions - trackId
          case ls => subscriptions + (trackId -> ls)
        }
    }

  private def getFinishedStates(tracked: Map[String, TrackingEntry]): Map[String, ExecutionFinishedState] =
    tracked.values.map {
      case TrackingEntry(st: ExecutionFinishedState, _) => Some((st.trackId, st))
      case _ => None
    }.flatten.toMap

  private def cleanUp(maxAge: org.joda.time.Duration, tracked: Map[String, TrackingEntry], subscriptions: Map[String, List[ActorRef]]): (Map[String, TrackingEntry], Map[String, List[ActorRef]]) = {
    val beforeIsExpired = canCreateUuidsAndDateTimes.getUtcTimestamp.minus(maxAge)
    val expired = tracked.values.filter(_.lastModified.compareTo(beforeIsExpired) < 0)
    val expiredTrackIds = expired.map(_.currentState.trackId).toSet
    subscriptions filterKeys (subscriptionTrackId =>
      expiredTrackIds contains (subscriptionTrackId)) foreach {
      case (trackId, subscribers) =>
        subscribers foreach (_ ! ExecutionTrackingExpired(trackId))
    }
    (tracked -- expiredTrackIds, subscriptions -- expiredTrackIds)
  }
}