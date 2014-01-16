package almhirt.testkit

import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.TestDuration
import org.scalatest._
import almhirt.core._

abstract class AlmhirtTestKit(theActorSystem: ActorSystem) extends TestKit(theActorSystem) with BeforeAndAfterAll with CreatesUniqueTestIds with HasAlmhirt { self : Suite =>
  def defaultDuration: scala.concurrent.duration.FiniteDuration = theAlmhirt.durations.shortDuration

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}