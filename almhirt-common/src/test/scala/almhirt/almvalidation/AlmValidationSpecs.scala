package almhirt.almvalidation

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import scalaz._, Scalaz._
import almhirt.common._

class AlmValidationSpecs extends FlatSpec with ShouldMatchers {
  import almhirt.almvalidation.funs._
  import almhirt.problem.inst._
  import almhirt.syntax.almvalidation._

  "AlmValidation.parseIntAlm" should
    "return a success of 1 when supplied with '1'" in {
      (parseIntAlm("1")) should equal(Success(1))
    }
  it should "return a failure when supplied with ' 1'" in {
    (parseIntAlm(" 1")).isFailure should be(true)
  }
  it should "return a failure when supplied with '1 '" in {
    (parseIntAlm("1 ")).isFailure
  }

  "AlmValidation.parseLongAlm" should
    "return a success of 1 when supplied with '1'" in {
      (parseIntAlm("1")) should equal(Success(1L))
    }
  it should "return a failure when supplied with ' 1'" in {
    (parseIntAlm(" 1")).isFailure should be(true)
  }
  it should "return a failure when supplied with '1 '" in {
    (parseIntAlm("1 ")).isFailure should be(true)
  }

  "AlmValidation.parseDoubleAlm" should
    "return a success of 1 when supplied with '1'" in {
      (parseDoubleAlm("1")) should equal(Success(1d))
    }
  it should "return a success of 1.1 when supplied with '1.1'" in {
    (parseDoubleAlm("1.1")) should equal(Success(1.1d))
  }
  it should "return a success of 0.1 when supplied with '.1'" in {
    (parseDoubleAlm(".1")) should equal(Success(0.1))
  }
  it should "return a success of 1 when supplied with '1 '" in {
    (parseDoubleAlm("1 ")) should equal(Success(1d))
  }
  it should "return a success of 1 when supplied with ' 1'" in {
    (parseDoubleAlm(" 1")) should equal(Success(1d))
  }
  it should "return a success of 1 when supplied with ' 1.0'" in {
    (parseDoubleAlm(" 1.0")) should equal(Success(1d))
  }
  it should "return a success of 1 when supplied with '1.0 '" in {
    (parseDoubleAlm("1.0 ")) should equal(Success(1d))
  }
  it should "return a failure when supplied with ''" in {
    (parseIntAlm("")).isFailure should be(true)
  }
  it should "return a failure when supplied with 'x'" in {
    (parseIntAlm("x")).isFailure should be(true)
  }
  it should "return a failure when supplied with 'a1.0'" in {
    (parseIntAlm("a1.0")).isFailure should be(true)
  }
  it should "return a failure when supplied with '1,0'" in {
    (parseIntAlm("1,0")).isFailure should be(true)
  }
  it should "return a failure when supplied with '1.0.0'" in {
    (parseIntAlm("1.0.0")).isFailure should be(true)
  }

  "AlmValidation.failIfEmpty" should
    """return a success of "x" when supplied with "x"""" in {
      (notEmpty("x")) should equal(Success("x"))
    }
  it should """return a success of " " when supplied with " """" in {
    (notEmpty(" ")) should equal(Success(" "))
  }
  it should """return a failure when supplied with "" """ in {
    (parseIntAlm("")).isFailure should be(true)
  }

  "AlmValidation.failIfEmptyOrWhitespace" should
    """return a success of "x" when supplied with "x"""" in {
      (notEmptyOrWhitespace("x")) should equal(Success("x"))
    }
  it should """return a failure when supplied with " """" in {
    (notEmptyOrWhitespace(" ")).isFailure should be(true)
  }
  it should """return a failure when supplied with """"" in {
    (notEmptyOrWhitespace("")).isFailure should be(true)
  }

  """Two strings(A,B) parsed to ints and lifted to AggregateProblem validations in a "for comprehension"""" should
    """add to 5 when A="2" and B="3"""" in {
      val res = parseIntAlm("2").toAgg flatMap (x => parseIntAlm("3").toAgg.map(_ + x))
      res should equal(Success(5))
    }
  it should """be a Failure when A="x" and B="3"""" in {
    val res = parseIntAlm("2").toAgg flatMap (_ => parseIntAlm("x").toAgg)
    res.isFailure should be(true)
  }

  """Two strings(A,B) parsed to ints and lifted to AggregateProblem validations in an applicative functor""" should
    """add to 5 when A="2" and B="3"""" in {
      val a = "2".toIntAlm().toAgg
      val b = "3".toIntAlm().toAgg
      val res = (a |@| b)((a, b) => a + b)
      res should equal(Success(5))
    }
  it should """be a Failue when A="x" and B="3"""" in {
    val a = "x".toIntAlm().toAgg
    val b = "3".toIntAlm().toAgg
    val res = (a |@| b)((a, b) => a + b)
    res.isFailure should be(true)
  }

  private class A
  private class B extends A
  private class C extends A

  """almCast (B and C inherit from A)""" should
    """cast A to A""" in {
      almCast[A](new A).isSuccess should be(true)
    }
  it should """cast B to B""" in {
    almCast[B](new B).isSuccess should be(true)
  }
  it should """cast C to C""" in {
    almCast[C](new C).isSuccess should be(true)
  }
  it should """cast B to A""" in {
    almCast[A](new B).isSuccess should be(true)
  }
  it should """cast C to A""" in {
    almCast[A](new C).isSuccess should be(true)
  }
  it should """not cast A to B""" in {
    almCast[B](new A).isFailure should be(true)
  }
  it should """not cast A to C""" in {
    almCast[C](new A).isFailure should be(true)
  }
  it should """cast a 1 as Any to Int""" in {
    val x: Any = 1
    almCast[Int](x).isSuccess should be(true)
  }
}
