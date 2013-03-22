package almhirt.almvalidation

import scalaz.syntax.validation._
import almhirt.common._


trait AlmConstraintsFuns {
  def notEmpty(toTest: String): AlmValidation[String] =
    if (toTest.isEmpty) BadDataProblem("String must not be empty").failure else toTest.success

  def notEmptyOrWhitespace(toTest: String): AlmValidation[String] =
    if (toTest.trim.isEmpty)
      BadDataProblem("String must not be empty or whitespaces").failure
    else
      toTest.success
  
 def stringConstrained(toTest: String, minLength: Option[Int], maxLength: Option[Int], emptyOrWhiteSpace: Boolean = false): AlmValidation[String] =
    for {
      _ <- if (emptyOrWhiteSpace) toTest.success else notEmptyOrWhitespace(toTest)
      _ <- minLength match {
        case Some(min) => if (min > toTest.length()) ConstraintViolatedProblem(s"min length is $min").failure else toTest.success
        case None => toTest.success
      }
      _ <- maxLength match {
        case Some(max) => if (max < toTest.length()) ConstraintViolatedProblem(s"max length is $max").failure else toTest.success
        case None => toTest.success
      }
    } yield toTest

  def numericConstrained[T](toTest: T, minimum: Option[T], maximum: Option[T])(implicit ops: Numeric[T]): AlmValidation[T] = {
    import scalaz.std._
    import ops._
    for {
      _ <- option.cata(minimum)(x => if (x > toTest) ConstraintViolatedProblem(s"minimum is ${minimum.get}").failure else ().success, ().success)
      _ <- option.cata(maximum)(x => if (x < toTest) ConstraintViolatedProblem(s"maximum is ${maximum.get}").failure else ().success, ().success)
    } yield toTest
  }

  def numericConstrainedToMin[T](toTest: T, minimum: T)(implicit ops: Numeric[T]): AlmValidation[T] = {
    import ops._
    if (minimum > toTest) ConstraintViolatedProblem(s"minimum is $minimum").failure else toTest.success
  }

  def numericConstrainedToMax[T](toTest: T, maximum: T)(implicit ops: Numeric[T]): AlmValidation[T] = {
    import ops._
    if (maximum < toTest) ConstraintViolatedProblem(s"maximum is maximum").failure else toTest.success
  }

  def numericConstrainedToMinMax[T](toTest: T, minimum: T, maximum: T)(implicit ops: Numeric[T]): AlmValidation[T] = {
    import ops._
    for {
      _ <- if (maximum < toTest) ConstraintViolatedProblem(s"maximum is maximum").failure else ().success
      _ <- if (minimum > toTest) ConstraintViolatedProblem(s"minimum is $minimum").failure else ().success
    } yield toTest

  }

}