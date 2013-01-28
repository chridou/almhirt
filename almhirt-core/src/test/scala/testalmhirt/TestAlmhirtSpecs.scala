package testalmhirt

import scala.concurrent.duration.Duration
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import almhirt.core.test._
import org.scalatest._
import almhirt.core.test.TestPerson

class TestAlmhirtSpecs extends FlatSpec with AlmhirtTestKit {
  private implicit val atMost = Duration(2, "s")
  "The TestAlmhirt with a blocking repository" should
    "create and retrieve person" in {
      inExtendedTestAlmhirt(new BlockingRepoCoreBootstrapper(this.defaultConf)) { implicit almhirt =>
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