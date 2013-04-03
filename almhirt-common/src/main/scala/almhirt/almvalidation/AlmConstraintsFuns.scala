package almhirt.almvalidation

import scalaz.syntax.validation._
import almhirt.common._


trait AlmConstraintsFuns {
  def mustFulfill[T](toTest: T, pred: T => Boolean, msg: String): AlmValidation[T] =
    if(pred(toTest)) toTest.success else ConstraintViolatedProblem(msg).failure
  
  def mustBeTrue(toTest: Boolean): AlmValidation[Boolean] =
    if(toTest) toTest.success else ConstraintViolatedProblem("Must be true").failure

  def mustBeFalse(toTest: Boolean): AlmValidation[Boolean] =
    if(!toTest) toTest.success else ConstraintViolatedProblem("Must be false").failure
    
  def notEmpty(toTest: String): AlmValidation[String] =
    if (toTest.isEmpty) BadDataProblem("String must not be empty").failure else toTest.success

  def collectionNotEmpty[T](toTest: Traversable[T]): AlmValidation[Traversable[T]] =
    if (toTest.isEmpty) BadDataProblem("Collection must not be empty").failure else toTest.success
    
    
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

    
  def stringMustContain(toTest: String, mustBeContained: String): AlmValidation[String] =
    mustFulfill[String](toTest, x => x.contains(mustBeContained), s""""$toTest" does not contain "$mustBeContained"""")

  def stringMustHaveLength(toTest: String, l: Int): AlmValidation[String] =
    mustFulfill[String](toTest, _.length == l, s""""$toTest" does not have length $l""")

  def stringMustBeContainedIn(toTest: String, in: Seq[String]): AlmValidation[String] =
    mustFulfill[String](toTest, _.contains(toTest), s""""$toTest" is not contained""")
    
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
    if (maximum < toTest) ConstraintViolatedProblem(s"maximum is $maximum").failure else toTest.success
  }

  def numericConstrainedToMinMax[T](toTest: T, minimum: T, maximum: T)(implicit ops: Numeric[T]): AlmValidation[T] = {
    import ops._
    for {
      _ <- if (maximum < toTest) ConstraintViolatedProblem(s"maximum is $maximum").failure else ().success
      _ <- if (minimum > toTest) ConstraintViolatedProblem(s"minimum is $minimum").failure else ().success
    } yield toTest
  }
  
  def stringMustBeEmail(toTest: String): AlmValidation[String] = {
    ???
  }

  def stringMustBePhoneNumber(toTest: String): AlmValidation[String] = {
    ???
  }
  
  def stringMustBeAllUpperCaseLetters(toTest: String): AlmValidation[String] = {
    if(!toTest.forall(c => c.isLetter && c.isUpper))
      ConstraintViolatedProblem(s"""All characters must be upper case letters but this is not the case for "$toTest"""").failure 
      else 
        toTest.success
  }

  def stringMustBeAllLowerCaseLetters(toTest: String): AlmValidation[String] = {
    if(!toTest.forall(c => c.isLetter && c.isLower))
      ConstraintViolatedProblem(s"""All characters must be lower case letters but this is not the case for "$toTest"""").failure 
      else 
        toTest.success
  }
  
}