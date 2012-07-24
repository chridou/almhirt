package almhirt.validation

import scalaz._
import Scalaz._
import scalaz.syntax.validation._
import scalaz.{Failure}
import org.specs2.mutable._
import org.specs2.mutable.Specification
import almhirt.validation._

class AlmValidationSpecs extends Specification {
  import AlmValidation._
  import Problem._
  
  "AlmValidation.parseIntAlm" should {
    "return a success of 1 when supplied with '1'" in {
      (parseIntAlm("1")) must beEqualTo(1.success[SingleBadDataProblem])
    }
    "return a failure when supplied with ' 1'" in {
      (parseIntAlm(" 1")).isFailure
    }
    "return a failure when supplied with '1 '" in {
      (parseIntAlm("1 ")).isFailure
    }
  }

  "AlmValidation.parseLongAlm" should {
    "return a success of 1 when supplied with '1'" in {
      (parseIntAlm("1")) must beEqualTo(1L.success[SingleBadDataProblem])
    }
    "return a failure when supplied with ' 1'" in {
      (parseIntAlm(" 1")).isFailure
    }
    "return a failure when supplied with '1 '" in {
      (parseIntAlm("1 ")).isFailure
    }
  }

  "AlmValidation.parseDoubleAlm" should {
    "return a success of 1 when supplied with '1'" in {
      (parseDoubleAlm("1")) must beEqualTo(1d.success[SingleBadDataProblem])
    }
    "return a success of 1.1 when supplied with '1.1'" in {
      (parseDoubleAlm("1.1")) must beEqualTo(1.1d.success[SingleBadDataProblem])
    }
    "return a success of 0.1 when supplied with '.1'" in {
      (parseDoubleAlm("1.1")) must beEqualTo(1.1d.success[SingleBadDataProblem])
    }
    "return a success of 1 when supplied with '1 '" in {
      (parseDoubleAlm("1 ")) must beEqualTo(1d.success[SingleBadDataProblem])
    }
    "return a success of 1 when supplied with ' 1'" in {
      (parseDoubleAlm(" 1")) must beEqualTo(1d.success[SingleBadDataProblem])
    }
    "return a success of 1 when supplied with '1'" in {
      (parseDoubleAlm("1 ")) must beEqualTo(1d.success[SingleBadDataProblem])
    }
    "return a success of 1 when supplied with ' 1.0'" in {
      (parseDoubleAlm(" 1.0")) must beEqualTo(1d.success[SingleBadDataProblem])
    }
    "return a success of 1 when supplied with '1.0 '" in {
      (parseDoubleAlm("1.0 ")) must beEqualTo(1d.success[SingleBadDataProblem])
    }
    "return a failure when supplied with ''" in {
      (parseIntAlm("")).isFailure
    }
    "return a failure when supplied with 'x'" in {
      (parseIntAlm("x")).isFailure
    }
    "return a failure when supplied with 'a1.0'" in {
      (parseIntAlm("a1.0")).isFailure
    }
    "return a failure when supplied with '1,0'" in {
      (parseIntAlm("1,0")).isFailure
    }
    "return a failure when supplied with '1.0.0'" in {
      (parseIntAlm("1.0.0")).isFailure
    }
  }

  "AlmValidation.failIfEmpty" should {
    """return a success of "x" when supplied with "x"""" in {
      (notEmpty("x")) must beEqualTo("x".success[SingleBadDataProblem])
    }
    """return a success of " " when supplied with " """" in {
      (notEmpty(" ")) must beEqualTo(" ".success[SingleBadDataProblem])
    }
    """return a failure when supplied with "" """ in {
      (parseIntAlm("")).isFailure
    }
  }

  "AlmValidation.failIfEmptyOrWhitespace" should {
    """return a success of "x" when supplied with "x"""" in {
      (notEmptyOrWhitespace("x")) must beEqualTo("x".success[SingleBadDataProblem])
    }
    """return a failure when supplied with """"" in {
      (notEmptyOrWhitespace(" ")).isFailure
    }
    """return a failure when supplied with "" """ in {
      (notEmptyOrWhitespace("")).isFailure
    }
  }
  
 
  """A SingleBadDataProblem lifted to a MultipleBadDataProblem""" should {
    """contain the origins message in keysAndMessages with the origins key""" in {
	  val a = (SingleBadDataProblem("XXX", "A").fail[Int]).toMBD
	  a match {
	    case Failure(mbdp) => mbdp.keysAndMessages must beEqualTo(Map(("A" -> "XXX")))
	    case _ => sys.error("")
	  }
    }
  }
  
  """Two strings(A,B) parsed to ints and lifted to MultipleBadData validations in a "for comprehension"""" should {
    """add to 5 when A="2" and B="3"""" in {
      val res =
	      for {
	        a <- parseIntAlm("2").toMBD
	        b <- parseIntAlm("3").toMBD
	      } yield a + b
      res must beEqualTo(5.success[MultipleBadDataProblem])
    }
    """be a Failure when A="x" and B="3"""" in {
      val res =
	      for {
	        a <- parseIntAlm("x").toMBD
	        b <- parseIntAlm("3").toMBD
	      } yield a + b
      res.isFailure
    }
  }

  """Two strings(A,B) parsed to ints and lifted to MultipleBadData validations in an applicative functor""" should {
    """add to 5 when A="2" and B="3"""" in {
      val a = "2".toIntAlm().toMBD
      val b = "3".toIntAlm().toMBD
	  val res = (a |@| b)((a, b) => a + b)
      res must beEqualTo(5.success[MultipleBadDataProblem])
    }
    """be a Failue when A="x" and B="3"""" in {
      val a = "x".toIntAlm().toMBD
      val b = "3".toIntAlm().toMBD
	  val res = (a |@| b)((a, b) => a + b)
      res.isFailure
    }
  }
}
