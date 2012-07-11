package almhirt.validation

import org.specs2.mutable._
import org.specs2.matcher._
import almhirt.validation.Problem.SingleBadDataProblem

class ProblemSpecs extends Specification {
  val bdpA = SingleBadDataProblem("Message A", "A", Minor)
  val bdpB = SingleBadDataProblem("Message B", "B", Major)
  
  "A SingleBadDataProblem with key = 'A' and message = 'Message A' when transformed to a MultipleBadDataProblem" should {
    "have keysAndValues = 'A->Message A' when received 'toMultipleBadData()'" in {
      bdpA.toMultipleBadData.keysAndMessages must beEqualTo(Map("A" -> "Message A"))
    }
    "have the same severity as the original SingleBadDataProblem" in {
      bdpA.toMultipleBadData.severity must beEqualTo(bdpA.severity)
    }
  }
  
  "A MultipleBadDataProblem created by SingleBadDataProblems A and B with different severities and different keys" should {
    "have a severity of 'Critical' when added" in {
      (bdpA add bdpB).severity must beEqualTo(Major)
    }
    "have 2 items in keysAndMessages: 'A->Message A' and 'B->Message B'" in {
      (bdpA add bdpB).keysAndMessages must beEqualTo(Map("A" -> "Message A", "B" -> "Message B"))
    }
    "have 2 items in keysAndMessages: 'A->Message A' and 'B->Message B'" in {
      (bdpA add bdpB).keysAndMessages must beEqualTo(Map("A" -> "Message A", "B" -> "Message B"))
    }
  }
  
  "A MultipleBadDataError having added an already contained key with a different message" should {
    "have the second key appended with '_' when added by 'withBadData'" in {
      (bdpA add bdpB).withBadData("A", "Message C")
      .keysAndMessages must beEqualTo(Map("A" -> "Message A", "B" -> "Message B", "A_" -> "Message C"))
    }
    "have the second key appended with '_' when added as a BadDataObject" in {
      (bdpA add bdpB).add(SingleBadDataProblem("Message C", "A"))
      .keysAndMessages must beEqualTo(Map("A" -> "Message A", "B" -> "Message B", "A_" -> "Message C"))
    }
    "have the second key appended with '_' when added(combineWith)gi as a MultipleBadDataObject" in {
      (bdpA add bdpB).combineWith(SingleBadDataProblem("Message C", "A").toMultipleBadData)
      .keysAndMessages must beEqualTo(Map("A" -> "Message A", "B" -> "Message B", "A_" -> "Message C"))
    }
    "keep the severity when a BadDataObject with lower severity is added" in {
      (bdpA add bdpB).add(SingleBadDataProblem("Message C", "A", Minor))
      .severity must beEqualTo((bdpA add bdpB).severity)
    }
    "keep the severity when a BadDataObject with same severity is added" in {
      (bdpA add bdpB).add(SingleBadDataProblem("Message C", "A", Major))
      .severity must beEqualTo((bdpA add bdpB).severity)
    }
    "take the severity of the BadDataObject with higher severity is added" in {
      (bdpA add bdpB).add(SingleBadDataProblem("Message C", "A", Critical))
      .severity must beEqualTo(Critical)
    }
    "keep the severity when a MultipleBadDataObject with lower severity is added(combineWith)" in {
      (bdpA add bdpB).combineWith(SingleBadDataProblem("Message C", "A", Minor).toMultipleBadData)
      .severity must beEqualTo((bdpA add bdpB).severity)
    }
    "keep the severity when a MultipleBadDataObject with same severity is added(combineWith)" in {
      (bdpA add bdpB).combineWith(SingleBadDataProblem("Message C", "A", Major).toMultipleBadData)
      .severity must beEqualTo((bdpA add bdpB).severity)
    }
    "take the severity of the MultipleBadDataObject with higher severity is added(combineWith)" in {
      (bdpA add bdpB).combineWith(SingleBadDataProblem("Message C", "A", Critical).toMultipleBadData)
      .severity must beEqualTo(Critical)
    }
    "maintain its severity when added by 'withBadData'" in {
      (bdpA add bdpB).withBadData("A", "Message C")
      .severity must beEqualTo((bdpA add bdpB).severity)
    }
  }
  
}