package almhirt.testkit

import akka.actor._
import almhirt.core.HasAlmhirt
import almhirt.components.ExecutionStateTracker
import almhirt.components.impl.{ ExecutionTrackerTemplate, TrackerWithoutSecondLevelStore }
import almhirt.messaging.MessagePublisher

trait CreatesExecutionTracker {
  def createExecutionTracker(testId: Int): (ActorRef, () => Unit)
  def createExecutionTracker(testId: Int, publishTo: MessagePublisher): (ActorRef, () => Unit)
}

trait CreatesCreatesInMemoryExecutionTracker extends CreatesExecutionTracker { self: akka.testkit.TestKit with HasAlmhirt =>
  override def createExecutionTracker(testId: Int): (ActorRef, () => Unit) =
    createExecutionTracker(testId, theAlmhirt.messageBus)

  override def createExecutionTracker(testId: Int, messagePublisher: MessagePublisher): (ActorRef, () => Unit) = {
    val props = Props(
      new ExecutionStateTracker with ExecutionTrackerTemplate with TrackerWithoutSecondLevelStore with Actor with ActorLogging {
        val publishTo = messagePublisher
        val canCreateUuidsAndDateTimes = theAlmhirt
        val executionContext = theAlmhirt.futuresExecutor
        val secondLevelMaxAskDuration = scala.concurrent.duration.FiniteDuration(3, "s")
        def receive: Receive = handleTrackingMessage
      })
    (system.actorOf(props, "execution_state_tracker_" + testId), () => ())
  }
}