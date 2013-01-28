package testalmhirt

import scala.concurrent.duration.Duration
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import test._
import org.scalatest._

class TestAlmhirtSpecs extends FlatSpec with TestAlmhirtKit {
  private implicit val atMost = Duration(2, "s")
  "The TestAlmhirt" should 
    "create and retrieve person" in {
      inTestAlmhirt { almhirt =>
        val id1 = almhirt.getUuid
        almhirt.executeTrackedCommand(NewTestPerson(id1, "Harry"), "1")
        almhirt.operationStateTracker.getResultFor(atMost)("1").awaitResult
        almhirt.repositories.getForAggregateRoot[TestPerson, TestPersonEvent]
          .forceResult
          .get(id1)
          .awaitResult
          .forceResult
          .id === id1
      }
  }
}