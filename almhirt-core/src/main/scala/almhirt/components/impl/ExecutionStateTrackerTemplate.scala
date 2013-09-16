package almhirt.components.impl

import org.joda.time.LocalDateTime
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.components.{ ExecutionStateTracker, ExecutionStateStore, ExecutionStateEntry }
import almhirt.commanding._
import almhirt.messaging.MessagePublisher
import almhirt.problem.{ Major, Minor }

trait ExecutionTrackerTemplate { actor: ExecutionStateTracker with Actor with ActorLogging =>
  import ExecutionStateTracker._
  import ExecutionStateStore._

  implicit def publishTo: MessagePublisher
  implicit def canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes
  implicit def executionContext: ExecutionContext
  def secondLevelStore: SecondLevelStore
  def secondLevelMaxAskDuration: scala.concurrent.duration.FiniteDuration
  def targetSize: Int
  def cleanUpThreshold: Int
  def cleanUpInterval: FiniteDuration

  private case class StateUpdate(newState: Map[String, ExecutionStateEntry])
  private case object CleanUp

  protected var lifetimeExpiredSubscriptions = 0L
  protected var lifetimeTotalSubscriptions = 0L
  
  protected var numStartedReceived = 0L
  protected var numInProcessReceived = 0L
  protected var numSuccessfulReceived = 0L
  protected var numFailedReceived = 0L
  
  
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
    case CleanUp =>
      val (newTracked, newSubscriptions) = cleanUp(tracked, subscriptions)
      context.become(waitingForTrackedStateUpdate(newTracked, newSubscriptions, updateRequests))
  }

  protected def idleState(tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, List[ActorRef]]): Receive = {
    case ExecutionStateChanged(_, st) =>
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
    case CleanUp =>
      val (newTracked, newSubscriptions) = cleanUp(tracked, subscriptions)
      context.become(idleState(newTracked, newSubscriptions))
  }

  override def handleTrackingMessage = idleState(Map.empty, Map.empty)

  private def handleIncomingExecutionState(incomingState: ExecutionState, tracked: Map[String, ExecutionStateEntry]) {
    updateReceivedCounters(incomingState)
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
        else {
          log.warning(s"""The new ExecutionState with tracking-id "${incomingState.trackId}" is not logically greater than the present execution state. Did you use an already used old tracking-id? No changes were applied.""")
          None
        }
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
      case None => 
        lifetimeTotalSubscriptions += 1
        subscriptions + (trackId -> (subscriber :: Nil))
      case Some(subscribers) =>
        if (subscribers.exists(_ == subscriber))
          subscriptions
        else {
          lifetimeTotalSubscriptions += 1
          subscriptions + (trackId -> (subscriber :: subscribers))
        }
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

  private def updateReceivedCounters(state: ExecutionState) {
     state match {
       case st: ExecutionStarted => numStartedReceived +=1L
       case st: ExecutionInProcess => numInProcessReceived +=1L
       case st: ExecutionFailed => numFailedReceived +=1L
       case st: ExecutionSuccessful => numSuccessfulReceived +=1L
     }    
  }  
    
  def requestCleanUp() {
    self ! CleanUp
  }

  private def cleanUp(tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, List[ActorRef]]): (Map[String, ExecutionStateEntry], Map[String, List[ActorRef]]) = {
    val res =
      if (tracked.size >= cleanUpThreshold) {
        val start = System.currentTimeMillis()
        val entriesOrderedByAge = tracked.values.toVector.sortBy(_.lastModified)
        val numToDrop = tracked.size - targetSize
        val keep = entriesOrderedByAge.drop(numToDrop)
        val discard = entriesOrderedByAge.take(numToDrop)
        val discardIds = discard.map(_.currentState.trackId).toSet
        val expiredSubscriptions = subscriptions filterKeys (subscriptionTrackId =>
          discard contains (subscriptionTrackId))
        expiredSubscriptions.foreach {
          case (trackId, subscribers) =>
            subscribers foreach (_ ! ExecutionTrackingExpired(trackId))
        }
        lifetimeExpiredSubscriptions += expiredSubscriptions.size
        val res = (tracked -- discardIds, subscriptions -- discardIds)
        
        val time = System.currentTimeMillis() - start
        log.info(s"""Removed ${discard.size} items in $time[ms].\nThe new state is ${res._1.size}(old: ${tracked.size}) items tracked and ${res._2.size}(old: ${subscriptions.size}) subscriptions.\n${expiredSubscriptions.size} subscriptions were expired.""")
        res
      } else {
        log.info(s"Nothing to clean up. Current state is ${tracked.size} items tracked and ${subscriptions.size} subscriptions.")
        (tracked, subscriptions)
      }
    actor.context.system.scheduler.scheduleOnce(cleanUpInterval)(requestCleanUp())
    res
  }

}
