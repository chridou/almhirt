package almhirt.almvalidation

import scalaz._
import Scalaz._
import scalaz.syntax.validation._
import scalaz.{Failure}
import org.specs2.mutable._
import org.specs2.mutable.Specification
import almhirt.common._

class AlmValidationSpecs extends Specification {
  import almhirt.almvalidation.funs._
  import almhirt.problem.inst._
  import almhirt.syntax.almvalidation._
  
  "AlmValidation.parseIntAlm" should {
    "return a success of 1 when supplied with '1'" in {
      (parseIntAlm("1")) must beEqualTo(Success(1))
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
      (parseIntAlm("1")) must beEqualTo(Success(1L))
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
      (parseDoubleAlm("1")) must beEqualTo(Success(1d))
    }
    "return a success of 1.1 when supplied with '1.1'" in {
      (parseDoubleAlm("1.1")) must beEqualTo(Success(1.1d))
    }
    "return a success of 0.1 when supplied with '.1'" in {
      (parseDoubleAlm(".1")) must beEqualTo(Success(0.1))
    }
    "return a success of 1 when supplied with '1 '" in {
      (parseDoubleAlm("1 ")) must beEqualTo(Success(1d))
    }
    "return a success of 1 when supplied with ' 1'" in {
      (parseDoubleAlm(" 1")) must beEqualTo(Success(1d))
    }
    "return a success of 1 when supplied with '1'" in {
      (parseDoubleAlm("1 ")) must beEqualTo(Success(1d))
    }
    "return a success of 1 when supplied with ' 1.0'" in {
      (parseDoubleAlm(" 1.0")) must beEqualTo(Success(1d))
    }
    "return a success of 1 when supplied with '1.0 '" in {
      (parseDoubleAlm("1.0 ")) must beEqualTo(Success(1d))
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
      (notEmpty("x")) must beEqualTo(Success("x"))
    }
    """return a success of " " when supplied with " """" in {
      (notEmpty(" ")) must beEqualTo(Success(" "))
    }
    """return a failure when supplied with "" """ in {
      (parseIntAlm("")).isFailure
    }
  }

  "AlmValidation.failIfEmptyOrWhitespace" should {
    """return a success of "x" when supplied with "x"""" in {
      (notEmptyOrWhitespace("x")) must beEqualTo(Success("x"))
    }
    """return a failure when supplied with """"" in {
      (notEmptyOrWhitespace(" ")).isFailure
    }
    """return a failure when supplied with "" """ in {
      (notEmptyOrWhitespace("")).isFailure
    }
  }
  
 
  
  """Two strings(A,B) parsed to ints and lifted to MultipleBadData validations in a "for comprehension"""" should {
    """add to 5 when A="2" and B="3"""" in {
      val res = parseIntAlm("2").toAgg flatMap (x => parseIntAlm("3").toAgg.map(_ + x))
      res must beEqualTo(Success(5))
    }
    """be a Failure when A="x" and B="3"""" in {
      val res = parseIntAlm("2").toAgg flatMap (_ => parseIntAlm("x").toAgg)
      res.isFailure
    }
  }

  """Two strings(A,B) parsed to ints and lifted to MultipleBadData validations in an applicative functor""" should {
    """add to 5 when A="2" and B="3"""" in {
      val a = "2".toIntAlm().toAgg
      val b = "3".toIntAlm().toAgg
	  val res = (a |@| b)((a, b) => a + b)
      res must beEqualTo(Success(5))
    }
    """be a Failue when A="x" and B="3"""" in {
      val a = "x".toIntAlm().toAgg
      val b = "3".toIntAlm().toAgg
	  val res = (a |@| b)((a, b) => a + b)
      res.isFailure
    }
  }
}
