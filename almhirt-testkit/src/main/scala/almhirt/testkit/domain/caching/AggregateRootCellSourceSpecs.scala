package almhirt.testkit.domain.caching

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import almhirt.core._
import almhirt.testkit._
import akka.actor.ActorSystem

abstract class AggregateRootCellSourceSpecs(theActorSystem: ActorSystem)  extends AlmhirtTestKit(theActorSystem) with HasAlmhirt with FunSpec with ShouldMatchers { self: CreatesEventLog =>

}