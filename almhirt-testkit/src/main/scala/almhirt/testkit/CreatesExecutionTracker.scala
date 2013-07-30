package almhirt.testkit

import akka.actor.ActorRef

trait CreatesExecutionTracker {
  def createExecutionTracker(testId: Int): (ActorRef, () => Unit)
}

trait CreatesCreatesInMemoryExecutionTracker extends CreatesExecutionTracker {  self: akka.testkit.TestKit =>
  
}