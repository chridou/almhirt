package almhirt.testkit

import almhirt.core._
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.TestDuration

abstract class AlmhirtTestKit(theActorSystem: ActorSystem) extends TestKit(theActorSystem) with CreatesUniqueTestIds with HasAlmhirt {
   
   def defaultDuration: scala.concurrent.duration.FiniteDuration = theAlmhirt.durations.shortDuration
}