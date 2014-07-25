package almhirt.testkit

import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.TestDuration
import org.scalatest._
import almhirt.core._

abstract class AlmhirtTestKit(theActorSystem: ActorSystem) extends TestKit(theActorSystem) with CreatesUniqueTestIds with HasAlmhirt { 
  def defaultDuration: scala.concurrent.duration.FiniteDuration = theAlmhirt.durations.shortDuration

  protected def shutdown {
    TestKit.shutdownActorSystem(system)
  }
}