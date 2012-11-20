package almhirt.eventlog.anorm

import org.specs2.mutable._
import test._

class FactorySpecs extends Specification with TestAlmhirtKit {
  "A anorm SerializingAnormEventLogFactory" should {
    "create an eventlog with an SerializingAnormEventLogActor when configured" in {
      inTestAlmhirt(implicit almhirt => {
        println(almhirt.environment.eventLog.actor)
        almhirt.environment.eventLog.actor.isInstanceOf[SerializingAnormEventLogActor]
      })
    }
  }

}