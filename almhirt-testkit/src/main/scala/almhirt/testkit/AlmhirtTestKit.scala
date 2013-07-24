package almhirt.testkit

import almhirt.core._
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.TestDuration

abstract class AlmhirtTestKit(theActorSystem: ActorSystem) extends TestKit(theActorSystem) with HasAlmhirt {
   private val currentTestId = new java.util.concurrent.atomic.AtomicInteger(1)
   def nextTestId = currentTestId.getAndIncrement()
   
   def defaultDuration: scala.concurrent.duration.FiniteDuration
}