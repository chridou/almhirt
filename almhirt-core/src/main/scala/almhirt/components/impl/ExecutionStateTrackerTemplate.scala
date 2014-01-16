package almhirt.components.impl

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.actor._
import almhirt.common._
import almhirt.core._
import almhirt.core.types._
import almhirt.components.{ ExecutionStateTracker, ExecutionStateStore, ExecutionStateEntry }
import almhirt.messaging.MessagePublisher
import almhirt.problem.{ Major, Minor }

trait ExecutionTrackerTemplate { actor: ExecutionStateTracker with Actor with ActorLogging =>
  import ExecutionStateTracker._

  implicit def publishTo: MessagePublisher
  implicit def canCreateUuidsAndDateTimes: CanCreateUuidsAndDateTimes
  implicit def futuresContext: ExecutionContext
  def numberCruncher: ExecutionContext
  def targetSize: Int
  def cleanUpThreshold: Int
  def cleanUpInterval: FiniteDuration

  def inDebugMode: Boolean
  def subscriptionsOkReportingThreshold: Int
  def stateChangedMessageWarningAge: FiniteDuration

  def checkSubscriptions: Option[(FiniteDuration, FiniteDuration, FiniteDuration)]

  protected var lifetimeExpiredSubscriptions = 0L
  protected var lifetimeTotalSubscriptions = 0L

  protected var numStartedReceived = 0L
  protected var numInProcessReceived = 0L
  protected var numSuccessfulReceived = 0L
  protected var numFailedReceived = 0L

  protected var numOldMessages = 0L

  private var deadlinesBySubscriptions = Map.empty[ActorRef, Deadline]
  private var trackingIdsBySubscription = Map.empty[ActorRef, String]

  private case object CleanUp
  private case object CheckSubscriptions

  protected def currentStateHandler(tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, Set[ActorRef]]): Receive = {
    case ExecutionStateChanged(header, incomingExecutionState) =>
      val newState = handleIncomingExecutionState(incomingExecutionState, tracked)
      val newSubscriptions = notifyFinishedStateSubscribers(newState, subscriptions)
      context.become(currentStateHandler(newState, newSubscriptions))
      val now = canCreateUuidsAndDateTimes.getUtcTimestamp
      val deadline = now.minusMillis(stateChangedMessageWarningAge.toMillis.toInt)
      if (header.timestamp.compareTo(deadline) < 0) {
        numOldMessages += 1
        val age = new org.joda.time.Period(header.timestamp, now)
        log.warning(s"""Received an execution state(${incomingExecutionState.getClass().getSimpleName()}(track id = "${incomingExecutionState.trackId}")) older than ${stateChangedMessageWarningAge.defaultUnitString}(${age.toString()}). The timestamp is ${header.timestamp.toString()}.""")
      }
    case GetExecutionStateFor(trackId) =>
      reportExecutionState(trackId, tracked, sender)
    case SubscribeForFinishedState(trackId) =>
      preSubscribe(trackId, sender, tracked) match {
        case Some(subscriber) => context.become(currentStateHandler(tracked, addSubscription(trackId, subscriber, subscriptions)))
        case None => ()
      }
    case UnsubscribeForFinishedState(trackId) =>
      context.become(currentStateHandler(tracked, removeSubscription(trackId, sender, subscriptions)))
    case CleanUp =>
      val (newTracked, newSubscriptions) = cleanUp(tracked, subscriptions)
      context.become(currentStateHandler(newTracked, newSubscriptions))
    case CheckSubscriptions =>
      checkSubscriptions(tracked, deadlinesBySubscriptions, trackingIdsBySubscription, subscriptions)
    case Terminated(subscriber) =>
      val trackId = trackingIdsBySubscription(subscriber)
      val age = deadlinesBySubscriptions(subscriber).lap
      val msg1 = s"""The subscription for tracking id "$trackId" "${subscriber.path.toString()}" died."""
      val msg2 = s"""The subscription died after ${age.defaultUnitString}."""
      val msg3 = tracked.get(trackId) match {
        case Some(entry) => s"""The tracked state is ${entry.toString()}"""
        case None => "The tracking id was not tracked"
      }
      log.warning(s"\n$msg1\n$msg2\n$msg3")
      val newSubscriptions = removeSubscription(trackId, sender, subscriptions)
      context.become(currentStateHandler(tracked, newSubscriptions))
  }

  override def handleTrackingMessage = currentStateHandler(Map.empty, Map.empty)

  private def handleIncomingExecutionState(incomingState: ExecutionState, tracked: Map[String, ExecutionStateEntry]): Map[String, ExecutionStateEntry] = {
    updateReceivedCounters(incomingState)
    getUpdatedState(incomingState, tracked) match {
      case None =>
        tracked
      case Some(updatedState) =>
        tracked + (updatedState.currentState.trackId -> updatedState)
    }
  }

  private def getUpdatedState(incomingState: ExecutionState, tracked: Map[String, ExecutionStateEntry]): Option[ExecutionStateEntry] = {
    tracked.get(incomingState.trackId) match {
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

  private def reportExecutionState(trackId: String, tracked: Map[String, ExecutionStateEntry], respondTo: ActorRef): Unit = {
    respondTo ! QueriedExecutionState(trackId, tracked.get(trackId).map(_.currentState))
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

  private def notifyFinishedStateSubscribers(tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, Set[ActorRef]]): Map[String, Set[ActorRef]] = {
    getFinishedStates(tracked).foldLeft(subscriptions) { (acc, cur) =>
      acc.get(cur._1) match {
        case Some(subscribers) =>
          subscribers foreach (subscriber => {
            subscriber ! FinishedExecutionStateResult(cur._2)
            removeFromSubscriptionsChecking(subscriber)
          })
          acc - cur._1
        case None => acc
      }
    }
  }

  private def addSubscription(trackId: String, subscriber: ActorRef, subscriptions: Map[String, Set[ActorRef]]): Map[String, Set[ActorRef]] = {
    val res = subscriptions.get(trackId) match {
      case None =>
        lifetimeTotalSubscriptions += 1
        addToSubscriptionsChecking(subscriber, trackId)
        subscriptions + (trackId -> Set(subscriber))
      case Some(subscribers) =>
        if (subscribers.contains(subscriber))
          subscriptions
        else {
          lifetimeTotalSubscriptions += 1
          addToSubscriptionsChecking(subscriber, trackId)
          subscriptions + (trackId -> (subscribers + subscriber))
        }
    }
    res
  }

  private def removeSubscription(trackId: String, subscriber: ActorRef, subscriptions: Map[String, Set[ActorRef]]): Map[String, Set[ActorRef]] =
    subscriptions get (trackId) match {
      case None => subscriptions
      case Some(subscribers) =>
        removeFromSubscriptionsChecking(subscriber)
        val newSubscribers = subscribers - subscriber
        if (newSubscribers.isEmpty)
          subscriptions - trackId
        else
          subscriptions + (trackId -> newSubscribers)
    }

  private def addToSubscriptionsChecking(subscriber: ActorRef, trackId: String) {
    this.context.watch(subscriber)
    deadlinesBySubscriptions = deadlinesBySubscriptions + (subscriber -> Deadline.now)
    trackingIdsBySubscription = trackingIdsBySubscription + (subscriber -> trackId)
  }

  private def removeFromSubscriptionsChecking(subscriber: ActorRef) {
    this.context.unwatch(subscriber)
    deadlinesBySubscriptions = deadlinesBySubscriptions - subscriber
    trackingIdsBySubscription = trackingIdsBySubscription - subscriber
  }

  private def getFinishedStates(tracked: Map[String, ExecutionStateEntry]): Map[String, ExecutionFinishedState] =
    tracked.values.map {
      case ExecutionStateEntry(st: ExecutionFinishedState, _) => Some((st.trackId, st))
      case _ => None
    }.flatten.toMap

  private def updateReceivedCounters(state: ExecutionState) {
    state match {
      case st: ExecutionStarted => numStartedReceived += 1L
      case st: ExecutionInProcess => numInProcessReceived += 1L
      case st: ExecutionFailed => numFailedReceived += 1L
      case st: ExecutionSuccessful => numSuccessfulReceived += 1L
    }
  }

  private def getStatsMsg(criticalIds: Iterable[String], tracked: Map[String, ExecutionStateEntry]): String = {
    if (criticalIds.isEmpty) {
      "There are no critical subsriptions."
    } else {
      var nUntracked = 0
      var nStarted = 0
      var nInProcess = 0
      var nExecuted = 0
      var nSuccess = 0
      var nFailed = 0
      criticalIds.foreach(trackId =>
        tracked get trackId match {
          case Some(ExecutionStateEntry(state, _)) =>
            state match {
              case e: ExecutionStarted =>
                nStarted += 1
              case e: ExecutionInProcess =>
                nInProcess += 1
              case e: ExecutionSuccessful =>
                nSuccess += 1
                nExecuted += 1
              case e: ExecutionFailed =>
                nFailed += 1
                nExecuted += 1
            }
          case None =>
            nUntracked += 1
        })
      s"""	|There are ${criticalIds.size} tracking states associated to the critical subscriptions. 
                		|nUntracked: $nUntracked
                		|nStarted: $nStarted
                		|nInProcess: $nInProcess
                		|nExecuted: $nExecuted
                		|nSuccess: $nSuccess
                		|nFailed: $nFailed""".stripMargin
    }

  }

  private def checkSubscriptions(tracked: Map[String, ExecutionStateEntry], deadlinesBySubscriptions: Map[ActorRef, Deadline], trackingIdsBySubscriptions: Map[ActorRef, String], subscriptions: Map[String, Set[ActorRef]]) {
    checkSubscriptions.foreach {
      case (interval, thresholdLvl1, thresholdLvl2) =>
        numberCruncher.execute(new Runnable {
          def run() {
            val expectedNumberOfSubscriptions = subscriptions.map(x => x._2.size).sum
            if (deadlinesBySubscriptions.size != expectedNumberOfSubscriptions)
              log.warning(s"deadlinesBySubscriptions should be $expectedNumberOfSubscriptions but is ${deadlinesBySubscriptions.size}")
            if (trackingIdsBySubscriptions.size != expectedNumberOfSubscriptions)
              log.warning(s"trackingIdsBySubscriptions should be $expectedNumberOfSubscriptions but is ${trackingIdsBySubscriptions.size}")

            val start = Deadline.now
            val deadline1 = start - thresholdLvl1
            val deadline2 = start - thresholdLvl2
            val criticalSubscriptions1 = deadlinesBySubscriptions.filter(_._2 < deadline1)
            val criticalSubscriptions2 = criticalSubscriptions1.filter(_._2 < deadline2)
            if (!(criticalSubscriptions1.isEmpty && criticalSubscriptions2.isEmpty)) {
              val nCritical1 = criticalSubscriptions1.size
              val nCritical2 = criticalSubscriptions2.size
              val percentage1 = (nCritical1.toDouble / expectedNumberOfSubscriptions.toDouble) * 100.0
              val percentage2 = (nCritical2.toDouble / expectedNumberOfSubscriptions.toDouble) * 100.0
              val msg1 = s"""There are $nCritical1($percentage1%) of $expectedNumberOfSubscriptions subscriptions older than ${thresholdLvl1.defaultUnitString} and $nCritical2($percentage2%) older than ${thresholdLvl2.defaultUnitString}"""
              val criticalTrackingIds1 = criticalSubscriptions1.map(x => trackingIdsBySubscriptions(x._1)).toSet
              val criticalTrackingIds2 = criticalSubscriptions2.map(x => trackingIdsBySubscriptions(x._1)).toSet
              val msg2 = s"Older than ${thresholdLvl1.defaultUnitString}:"
              val msg3 = getStatsMsg(criticalTrackingIds1, tracked)
              val msg4 = s"Older than ${thresholdLvl2.defaultUnitString}:"
              val msg5 = getStatsMsg(criticalTrackingIds2, tracked)
              val msg6 = "Hint: The state may have changed during the calculation of these values."
              val msg7 = s"""This calculation took ${start.lap.defaultUnitString}."""
              log.warning(s"\n$msg1\n$msg2\n$msg3\n$msg4\n$msg5\n$msg6\n$msg7")
            } else {
              if (expectedNumberOfSubscriptions >= subscriptionsOkReportingThreshold)
                log.info(s"All subscriptions($expectedNumberOfSubscriptions) are ok.")
            }
            actor.context.system.scheduler.scheduleOnce(interval)(requestSubscriptionChecking())
          }
        })
    }
  }

  def requestCleanUp() {
    self ! CleanUp
  }

  def requestSubscriptionChecking() {
    self ! CheckSubscriptions
  }

  private def cleanUp(tracked: Map[String, ExecutionStateEntry], subscriptions: Map[String, Set[ActorRef]]): (Map[String, ExecutionStateEntry], Map[String, Set[ActorRef]]) = {
    val res =
      if (tracked.size >= cleanUpThreshold) {
        val start = Deadline.now
        val entriesOrderedByAge = tracked.values.toVector.sortBy(_.lastModified)
        val numToDrop = tracked.size - targetSize
        val keep = entriesOrderedByAge.drop(numToDrop)
        val discard = entriesOrderedByAge.take(numToDrop)
        val discardIds = discard.map(_.currentState.trackId).toSet
        val expiredSubscriptions = subscriptions filterKeys (subscriptionTrackId =>
          discard contains (subscriptionTrackId))
        expiredSubscriptions.foreach {
          case (trackId, subscribers) =>
            subscribers foreach (subscriber => {
              subscriber ! ExecutionStateTrackingFailed(trackId, OperationTimedOutProblem("The subscription was cancelled because there were too many other subscriptions."))
              removeFromSubscriptionsChecking(subscriber)
            })
        }
        lifetimeExpiredSubscriptions += expiredSubscriptions.size
        val res = (tracked -- discardIds, subscriptions -- discardIds)

        val elapsed = start.lap
        log.info(s"""Removed ${discard.size} items in ${elapsed.defaultUnitString}.\nThe new state is ${res._1.size}(old: ${tracked.size}) items tracked and ${res._2.size}(old: ${subscriptions.size}) subscriptions.\n${expiredSubscriptions.size} subscriptions were expired.""")
        res
      } else {
        log.info(s"Nothing to clean up. Current state is ${tracked.size} items tracked and ${subscriptions.size} subscriptions.")
        (tracked, subscriptions)
      }
    if (!actor.context.system.isTerminated)
      actor.context.system.scheduler.scheduleOnce(cleanUpInterval)(requestCleanUp())
    res
  }

}
