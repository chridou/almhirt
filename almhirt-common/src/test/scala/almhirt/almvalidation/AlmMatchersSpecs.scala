package almhirt.almvalidation

import org.specs2.mutable.Specification
import scalaz.syntax.validation.ToValidationV
import almhirt._

class AlmMatchersSpecs extends Specification with ToAlmValidationOps{
  """A Success 1""" should {
    """match AlmSuccess(1)""" in {
      val validation = 1.success[UnspecifiedProblem]
      validation.m match {
        case AlmSuccess(_) => true
        case AlmFailure(_) => false
      }
    }
  }
}