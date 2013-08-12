package almhirt.components.impl

import org.joda.time.LocalDateTime
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.components.{ExecutionStateTracker, ExecutionStateStore, ExecutionStateEntry}
import almhirt.commanding._
import almhirt.messaging.MessagePublisher
import almhirt.problem.{ Major, Minor }

object ExecutionTrackerTemplate {

}

trait ExecutionTrackerTemplate { actor: ExecutionStateTracker with Actor with ActorLogging =>
  import ExecutionStateTracker._
  import ExecutionTrackerTemplate._
  import ExecutionStateStore._

  implicit def publishTo: MessagePublisher
  implicit def canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes
  implicit def executionContext: ExecutionContext
  def secondLevelStore: SecondLevelStore
  def secondLevelMaxAskDuration: scala.concurrent.duration.FiniteDuration

  private case class StateUpdate(newState: Map[String, ExecutionStateEntry])

  protected def waitingForTrackedStateUpdate(tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, List[ActorRef]], updateRequests: Vector[ExecutionState]): Receive = {
    case StateUpdate(newState) =>
      val newSubscriptions = notifyFinishedStateSubscribers(newState, subscriptions)
      if (updateRequests.isEmpty)
        context.become(idleState(newState, newSubscriptions))
      else {
        handleIncomingExecutionState(updateRequests.head, newState)
        context.become(waitingForTrackedStateUpdate(newState, subscriptions, updateRequests.tail))
      }
    case ExecutionStateChanged(_, st) =>
      log.debug(s"""Received ExecutionStateChanged: ${st.toString()}""")
      context.become(waitingForTrackedStateUpdate(tracked, subscriptions, updateRequests :+ st))
    case GetExecutionStateFor(trackId) =>
      reportExecutionState(trackId, tracked, sender)
    case SubscribeForFinishedState(trackId) =>
      preSubscribe(trackId, sender, tracked) match {
        case Some(subscriber) => context.become(waitingForTrackedStateUpdate(tracked, addSubscription(trackId, subscriber, subscriptions), updateRequests))
        case None => ()
      }
    case UnsubscribeForFinishedState(trackId) =>
      context.become(waitingForTrackedStateUpdate(tracked, removeSubscription(trackId, sender, subscriptions), updateRequests))
    case RemoveOldExecutionStates(maxAge) =>
      val (newTracked, newSubscriptions) = cleanUp(maxAge, tracked, subscriptions)
      context.become(waitingForTrackedStateUpdate(newTracked, newSubscriptions, updateRequests))
  }

  protected def idleState(tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, List[ActorRef]]): Receive = {
    case ExecutionStateChanged(_, st) =>
      log.debug(s"""Received ExecutionStateChanged: ${st.toString()}""")
      handleIncomingExecutionState(st, tracked)
      context.become(waitingForTrackedStateUpdate(tracked, subscriptions, Vector.empty))
    case GetExecutionStateFor(trackId) =>
      reportExecutionState(trackId, tracked, sender)
    case SubscribeForFinishedState(trackId) =>
      preSubscribe(trackId, sender, tracked) match {
        case Some(subscriber) => context.become(idleState(tracked, addSubscription(trackId, subscriber, subscriptions)))
        case None => ()
      }
    case UnsubscribeForFinishedState(trackId) =>
      context.become(idleState(tracked, removeSubscription(trackId, sender, subscriptions)))
    case RemoveOldExecutionStates(maxAge) =>
      val (newTracked, newSubscriptions) = cleanUp(maxAge, tracked, subscriptions)
      context.become(idleState(newTracked, newSubscriptions))
  }
  
  override def handleTrackingMessage = idleState(Map.empty, Map.empty)

  private def handleIncomingExecutionState(incomingState: ExecutionState, tracked: Map[String, ExecutionStateEntry]) {
    getUpdatedState(incomingState, tracked).fold(
      fail => 
        publishTo.publish(FailureEvent(s"""Could not determine the state to update for tracking id "${incomingState.trackId}"""", fail, Minor)),
      potUpdatedState => {
        potUpdatedState match {
          case Some(updatedState) =>
            self ! StateUpdate(tracked + (updatedState.currentState.trackId -> updatedState))
          case None =>
            self ! StateUpdate(tracked)
        }
      })
  }

  private def getUpdatedState(incomingState: ExecutionState, tracked: Map[String, ExecutionStateEntry]): AlmFuture[Option[ExecutionStateEntry]] = {
    getExecutionState(incomingState.trackId, tracked).map {
      case None =>
        Some(ExecutionStateEntry(incomingState))
      case Some(oldState) =>
        if (ExecutionState.compareExecutionState(incomingState, oldState.currentState) > 0)
          Some(ExecutionStateEntry(incomingState))
        else
          None
    }
  }

  private def getExecutionState(trackId: String, tracked: Map[String, ExecutionStateEntry]): AlmFuture[Option[ExecutionStateEntry]] =
    (tracked.get(trackId) match {
      case Some(entry) => AlmFuture.successful(Some(entry))
      case None => secondLevelStore.get(trackId)(secondLevelMaxAskDuration)
    })

  private def reportExecutionState(trackId: String, tracked: Map[String, ExecutionStateEntry], respondTo: ActorRef): Unit = {
    val pinnedSender = respondTo
    getExecutionState(trackId, tracked).onComplete(
      fail => {
        pinnedSender ! QueriedExecutionState(trackId, None)
        publishTo.publish(FailureEvent(s"""Could not get the execution state for tracking id "$trackId".""", fail, Major))
      },
      succ => pinnedSender ! QueriedExecutionState(trackId, succ.map(_.currentState)))
  }

  private def preSubscribe(trackId: String, subscriber: ActorRef, tracked: Map[String, ExecutionStateEntry]): Option[ActorRef] = {
    getFinishedStates(tracked).get(trackId) match {
      case None =>
        Some(subscriber)
      case Some(finishedState) =>
        subscriber ! FinishedExecutionStateResult(finishedState)
        None
    }
  }

  private def notifyFinishedStateSubscribers(tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, List[ActorRef]]): Map[String, List[ActorRef]] = {
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

  private def getFinishedStates(tracked: Map[String, ExecutionStateEntry]): Map[String, ExecutionFinishedState] =
    tracked.values.map {
      case ExecutionStateEntry(st: ExecutionFinishedState, _) => Some((st.trackId, st))
      case _ => None
    }.flatten.toMap

  private def cleanUp(maxAge: org.joda.time.Duration, tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, List[ActorRef]]): (Map[String, ExecutionStateEntry], Map[String, List[ActorRef]]) = {
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

trait TrackerWithoutSecondLevelStore { self: ExecutionTrackerTemplate =>
  import ExecutionStateTracker._
  import ExecutionStateStore._
  
  override final val secondLevelMaxAskDuration = scala.concurrent.duration.FiniteDuration(10, "ms")

  override val secondLevelStore = new SecondLevelStore {
     override def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[ExecutionStateEntry]] =
       AlmFuture.successful(None)
  }

}