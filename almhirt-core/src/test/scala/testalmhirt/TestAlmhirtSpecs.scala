package testalmhirt

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scala.concurrent.duration.Duration
import almhirt._
import almhirt.syntax.almvalidation._
import almhirt.environment._
import almhirt.core.test._
import almhirt.core.test.TestPerson

class TestAlmhirtSpecs extends FlatSpec with ShouldMatchers with AlmhirtTestKit {
  private implicit val atMost = Duration(2, "s")
  "The TestAlmhirt with a blocking repository" should
    "create and retrieve person" in {
      inExtendedTestAlmhirt(new BlockingRepoCoreBootstrapper(this.defaultConf)) { implicit almhirt =>
        val id1 = almhirt.getUuid
        almhirt.executeTrackedCommand(TestPersonCommand.createCreator(NewTestPersonAction(id1, "Harry")), "1")
        val res = almhirt.operationStateTracker.getResultFor(atMost)("1").awaitResult
        println(res)
        almhirt.repositories.getForAggregateRoot[TestPerson, TestPersonEvent]
          .forceResult
          .get(id1)
          .awaitResult
          .forceResult
          .id should equal(id1)
      }
    }
}