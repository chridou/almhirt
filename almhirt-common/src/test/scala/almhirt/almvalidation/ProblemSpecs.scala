package almhirt.almvalidation

import org.specs2.mutable._
import org.specs2.matcher._
import almhirt.common._

class ProblemSpecs extends Specification with almhirt.problem.ProblemInstances {
  import almhirt.almvalidation.kit._
  val bdpA = BadDataProblem("Message A", Minor).toAggregate
  val bdpB = BadDataProblem("Message B", Major).toAggregate
  
  "A MultipleBadDataError having added an already contained key with a different message" should {
    "keep the severity when a BadDataObject with lower severity is added" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Minor).toAggregate)
      .severity must beEqualTo((bdpA addProblem bdpB).severity)
    }
    "keep the severity when a BadDataObject with same severity is added" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Major).toAggregate)
      .severity must beEqualTo((bdpA addProblem bdpB).severity)
    }
    "take the severity of the BadDataObject with higher severity is added" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Critical).toAggregate)
      .severity must beEqualTo(Critical)
    }
    "keep the severity when a MultipleBadDataObject with lower severity is added(combineWith)" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Minor).toAggregate)
      .severity must beEqualTo((bdpA addProblem bdpB).severity)
    }
    "keep the severity when a MultipleBadDataObject with same severity is added(combineWith)" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Major).toAggregate)
      .severity must beEqualTo((bdpA addProblem bdpB).severity)
    }
    "take the severity of the MultipleBadDataObject with higher severity is added(combineWith)" in {
      (bdpA addProblem bdpB).addProblem(BadDataProblem("Message C", Critical).toAggregate)
      .severity must beEqualTo(Critical)
    }
  }
  
}