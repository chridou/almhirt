package testalmhirt

import org.specs2.mutable._
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import test._

class TestAlmhirtSpecs extends Specification with TestAlmhirtKit {
  private implicit val atMost = akka.util.Duration(2, "s")
  "The TestAlmhirt" should {
    "create and retrieve person" in {
      inTestAlmhirt{almhirt =>
        val id1 = almhirt.getUuid
        almhirt.executeTrackedCommand(NewTestPerson(id1, "Harry"), "1")
        almhirt.operationStateTracker.getResultFor("1").awaitResult
        almhirt.repositories.getForAggregateRoot[TestPerson, TestPersonEvent]
          .forceResult
          .get(id1)
          .awaitResult
          .forceResult
          .id === id1
      }
    }
  }
}