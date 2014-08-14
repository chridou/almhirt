package almhirt.testkit

import akka.testkit.TestProbe
import akka.actor._
import almhirt.core.HasAlmhirt
import almhirt.messaging.MessagePublisher
import almhirt.components.CommandEndpoint

trait CreatesCommandEndpoint { self: akka.testkit.TestKit ⇒
  // endpoint, publishTo-Spy, cleanup
  def createCommandEndpoint(testId: Int, getTrackingId: () ⇒ String): (CommandEndpoint, TestProbe, ActorRef, () ⇒ Unit)
  def createCommandEndpoint(testId: Int, tracker: ActorRef): (CommandEndpoint, TestProbe, ActorRef, () ⇒ Unit)
  def createCommandEndpoint(testId: Int): (CommandEndpoint, TestProbe, ActorRef, () ⇒ Unit)
  def createCommandEndpoint(testId: Int, tracker: ActorRef, getTrackingId: () ⇒ String): (CommandEndpoint, TestProbe, ActorRef, () ⇒ Unit)
}

trait CreatesDefaultCommandEndpoint extends CreatesCommandEndpoint { self: akka.testkit.TestKit with HasAlmhirt with CreatesExecutionTracker ⇒
  override def createCommandEndpoint(testId: Int, tracker: ActorRef): (CommandEndpoint, TestProbe, ActorRef, () ⇒ Unit) = {
    val spy = TestProbe()
    val publishTo = MessagePublisher.sendToActor(spy.ref)
    val endpoint = CommandEndpoint(publishTo, tracker)(theAlmhirt, theAlmhirt.futuresExecutor)
    (endpoint, spy, tracker, () ⇒ { system.stop(spy.ref) })
  }

  override def createCommandEndpoint(testId: Int, tracker: ActorRef, getTrackingId: () ⇒ String): (CommandEndpoint, TestProbe, ActorRef, () ⇒ Unit) = {
    val spy = TestProbe()
    val publishTo = MessagePublisher.sendToActor(spy.ref)
    val endpoint = CommandEndpoint(publishTo, tracker, getTrackingId)(theAlmhirt, theAlmhirt.futuresExecutor)
    (endpoint, spy, tracker, () ⇒ { system.stop(spy.ref) })
  }
  
  override def createCommandEndpoint(testId: Int): (CommandEndpoint, TestProbe, ActorRef, () ⇒ Unit) = {
    val spy = TestProbe()
    val publishTo = MessagePublisher.sendToActor(spy.ref)
    val (tracker, trackerCleanUp) = createExecutionTracker(testId)
    val endpoint = CommandEndpoint(publishTo, tracker)(theAlmhirt, theAlmhirt.futuresExecutor)
    (endpoint, spy, tracker, () ⇒ { trackerCleanUp(); system.stop(spy.ref) })
  }
  
  override def createCommandEndpoint(testId: Int, getTrackingId: () ⇒ String): (CommandEndpoint, TestProbe, ActorRef, () ⇒ Unit) = {
    val spy = TestProbe()
    val publishTo = MessagePublisher.sendToActor(spy.ref)
    val (tracker, trackerCleanUp) = createExecutionTracker(testId)
    val endpoint = CommandEndpoint(publishTo, tracker, getTrackingId)(theAlmhirt, theAlmhirt.futuresExecutor)
    (endpoint, spy, tracker, () ⇒ { trackerCleanUp(); system.stop(spy.ref) })
  }
  
}