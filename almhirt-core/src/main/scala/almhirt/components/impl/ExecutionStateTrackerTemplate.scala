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
import almhirt.problem.Major

object ExecutionTrackerTemplate {
  final case class TrackingEntry(lastModified: LocalDateTime, currentState: ExecutionState) {
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

  trait PersistentStoreWrapper {
    def store(entry: TrackingEntry)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Unit]
    def get(trackId: String)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Option[TrackingEntry]]
    def getAllYoungerThan(age: org.joda.time.Duration)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Seq[TrackingEntry]]
    def removeAllOlderThan(age: org.joda.time.Duration)(atMost: scala.concurrent.duration.FiniteDuration): AlmFuture[Unit]
  }

  object PersistentStoreWrapper {
    sealed trait PersistentStoreWrapperMessage
    sealed trait StoreEntryResponse
    final case class StoreEntry(entry: TrackingEntry) extends PersistentStoreWrapperMessage
    final case class StoreEntryState(problem: Option[Problem]) extends StoreEntryResponse
    sealed trait GetEntryResponse extends PersistentStoreWrapperMessage
    final case class GetEntry(trackId: String) extends PersistentStoreWrapperMessage
    final case class GetEntryResult(entry: Option[TrackingEntry]) extends GetEntryResponse
    final case class GetEntryFailure(problem: Problem) extends GetEntryResponse
    sealed trait GetAllYoungerThanResponse extends PersistentStoreWrapperMessage
    final case class GetAllYoungerThan(age: org.joda.time.Duration) extends PersistentStoreWrapperMessage
    final case class GetAllYoungerThanResult(entries: Seq[TrackingEntry]) extends GetAllYoungerThanResponse
    final case class GetAllYoungerThanFailure(problem: Problem) extends GetAllYoungerThanResponse
    sealed trait RemoveAllOlderThanResponse extends PersistentStoreWrapperMessage
    final case class RemoveAllOlderThan(age: org.joda.time.Duration) extends PersistentStoreWrapperMessage
    final case class RemoveAllOlderThanState(problem: Option[Problem]) extends RemoveAllOlderThanResponse

    import scalaz.syntax.validation._
    import akka.pattern.ask
    import akka.util.Timeout
    import almhirt.almfuture.all._

    def apply(actor: ActorRef)(implicit executionContext: ExecutionContext): PersistentStoreWrapper = {
      new PersistentStoreWrapper {
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
  def persistentStore: PersistentStoreWrapper
  def persistentStoreMaxDuration: FiniteDuration
  def maxAgeInPersistenStore: Option[org.joda.time.Duration]
  def maxStartupAge: Option[org.joda.time.Duration]

  protected case class SubscriptionEntry(subscriber: ActorRef, expires: LocalDateTime)
  
//  protected case class FirstLevelState(tracked: Map[String, TrackingEntry], subscriptionsForFinished: Map[String, Map[ActorRef, LocalDateTime]]) {
//    def addSubscriber(trackId: String, maxWaitTime: FiniteDuration, subscriber: ActorRef): FirstLevelState = {
//      val subscription = (subscriber, canCreateUuidsAndDateTimes.getUtcTimestamp.plusMillis(maxWaitTime.toMillis.toInt))
//      subscriptionsForFinished.get(trackId) match {
//        case None =>
//          FirstLevelState(this.tracked, this.subscriptionsForFinished + (trackId -> Map(subscription)))
//        case Some(subscriptionForTrackId) =>
//          FirstLevelState(this.tracked, this.subscriptionsForFinished + (trackId -> (subscriptionForTrackId + (subscription))))
//      }
//    }
//
//    def getSubscriptions(trackId: String): Iterable[ActorRef] =
//      subscriptionsForFinished.get(trackId).map(_.keys).getOrElse(Iterable.empty)
//
//    def removeAllFor(trackId: String): FirstLevelState =
//      FirstLevelState(this.tracked - trackId, this.subscriptionsForFinished - trackId)
//
//    def removeSubscribersFor(trackId: String): FirstLevelState =
//      FirstLevelState(this.tracked, this.subscriptionsForFinished - trackId)
//
//    def potentiallyChangeState(executionState: ExecutionState): FirstLevelState =
//      tracked.get(executionState.trackId) match {
//        case None =>
//          FirstLevelState(this.tracked + (executionState.trackId -> TrackingEntry(canCreateUuidsAndDateTimes.getUtcTimestamp, executionState)), this.subscriptionsForFinished)
//        case Some(entry) =>
//          if (ExecutionState.compareExecutionState(executionState, entry.currentState) > 0) {
//            val newEntry = TrackingEntry(canCreateUuidsAndDateTimes.getUtcTimestamp, executionState)
//            FirstLevelState(this.tracked + (executionState.trackId -> newEntry), this.subscriptionsForFinished)
//          } else
//            this
//      }
//
//    def getAllFinishedStatesWithSubscribers: Iterable[(ExecutionFinishedState, Iterable[ActorRef])] = {
//      val finishedStates = tracked.map(_._2).map(_.tryGetFinished).flatten
//      finishedStates.map(fState =>
//        (fState, subscriptionsForFinished.get(fState.trackId).map(_.keys).getOrElse(Vector.empty)))
//    }
//  }

  private def postProcess(currentState: FirstLevelState) {
    val finishedWithSubscribers = currentState.getAllFinishedStatesWithSubscribers
    finishedWithSubscribers.foreach {
      case (state, subscribers) =>
        subscribers.foreach(_ ! FinishedExecutionStateResult(state.trackId, state))
    }
    val nextState = finishedWithSubscribers.foldLeft(currentState)((acc, cur) =>
      acc.removeSubscribersFor(cur._1.trackId))
    context.become(transitionToNextState(nextState))
  }

  private def getExecutionState(trackId: String, fromState: FirstLevelState): AlmFuture[Option[TrackingEntry]] =
    (fromState.tracked.get(trackId) match {
      case Some(entry) => AlmFuture.successful(Some(entry))
      case None => persistentStore.get(trackId)(persistentStoreMaxDuration)
    })

  private def reportExecutionState(trackId: String, fromState: FirstLevelState, respondTo: ActorRef): Unit = {
    val pinnedSender = respondTo
    getExecutionState(trackId, fromState).onComplete(
      fail => {
        pinnedSender ! CurrentExecutionState(trackId, None)
        publishTo.publish(FailureEvent(s"""Could not get the execution state for tracking id "$trackId".""", fail, Major))
      },
      succ => sender ! CurrentExecutionState(trackId, succ.map(_.currentState)))
  }

  
//  protected def 
  
  
  protected def idleState(tracked: Map[String, TrackingEntry]): Receive = {
    case GetExecutionStateFor(trackId) =>
      reportExecutionState(trackId, currentState, sender)
  }

  //  protected def transitionToNextState(currentState: FirstLevelState): Receive = {
  //    case st: ExecutionState =>
  //      val nextState = currentState.potentiallyChangeState(st)
  //      postProcess(nextState)
  //    case GetExecutionStateFor(trackId) =>
  //      postProcess(currentState)
  //    case RegisterForFinishedState(trackId, toRegister, maxWaitTime) =>
  //      val nextState = currentState.addSubscriber(trackId, maxWaitTime, toRegister)
  //      postProcess(nextState)
  //
  //  }

}