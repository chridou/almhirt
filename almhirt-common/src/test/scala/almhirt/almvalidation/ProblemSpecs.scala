package almhirt.almvalidation

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import almhirt.common._

class ProblemSpecs extends FlatSpec with ShouldMatchers with almhirt.problem.ProblemInstances {
  import almhirt.almvalidation.kit._
  val bdpA = BadDataProblem("Message A", Minor).toAggregate
  val bdpB = BadDataProblem("Message B", Major).toAggregate
  
  "An AggregateProblem having added a BadDataProblem" should 
    "keep the severity when a BadDataObject with lower severity is added" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Minor).toAggregate)
      .severity should equal((bdpA addProblem bdpB).severity)
    }
    it should "keep the severity when a BadDataObject with same severity is added" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Major).toAggregate)
      .severity should equal((bdpA addProblem bdpB).severity)
    }
    it should "take the severity of the BadDataObject with higher severity is added" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Critical).toAggregate)
      .severity should equal(Critical)
    }
    it should "keep the severity when a MultipleBadDataObject with lower severity is added(combineWith)" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Minor).toAggregate)
      .severity should equal((bdpA addProblem bdpB).severity)
    }
    it should "keep the severity when a MultipleBadDataObject with same severity is added(combineWith)" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Major).toAggregate)
      .severity should equal((bdpA addProblem bdpB).severity)
    }
    it should "take the severity of the MultipleBadDataObject with higher severity is added(combineWith)" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Critical).toAggregate)
      .severity should equal(Critical)
    }
  
}