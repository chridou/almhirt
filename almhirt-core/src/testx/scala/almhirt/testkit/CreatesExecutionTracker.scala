package almhirt.testkit

import akka.actor._
import almhirt.almvalidation.kit._
import almhirt.core.HasAlmhirt
import almhirt.components.ExecutionStateTracker
import almhirt.messaging.MessagePublisher

trait CreatesExecutionTracker {
  def createExecutionTracker(testId: Int): (ActorRef, () => Unit)
  def createExecutionTracker(testId: Int, publishTo: MessagePublisher): (ActorRef, () => Unit)
}

trait CreatesInMemoryExecutionTracker extends CreatesExecutionTracker { self: akka.testkit.TestKit with HasAlmhirt =>
  override def createExecutionTracker(testId: Int): (ActorRef, () => Unit) =
    createExecutionTracker(testId, theAlmhirt.messageBus)

  override def createExecutionTracker(testId: Int, messagePublisher: MessagePublisher): (ActorRef, () => Unit) = {
    val props = ExecutionStateTracker.props(theAlmhirt).resultOrEscalate
    (system.actorOf(props, "execution_state_tracker_" + testId), () => ())
  }
}