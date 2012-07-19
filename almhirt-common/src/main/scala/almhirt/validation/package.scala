package almhirt

import scalaz.{Validation, Success, Failure, ValidationNEL}
import scalaz.syntax.validation._
import almhirt.validation.Problem._

package object validation {
//  type AlmValidation[+α] = ({type λ[α] = Validation[Problem, α]})#λ[α]
//  type AlmValidationSingleBadData[+α] = ({type λ[α] = Validation[SingleBadDataProblem, α]})#λ[α]
//  type AlmValidationMultipleBadData[+α] = ({type λ[α] = Validation[MultipleBadDataProblem, α]})#λ[α]
  type AlmValidation[+α] = Validation[Problem, α]
  type AlmValidationSingleBadData[+α] = Validation[SingleBadDataProblem, α]
  type AlmValidationMultipleBadData[+α] = Validation[MultipleBadDataProblem, α]

object AlmValidation extends AlmValidationImplicits {
  import Problem._  
    
  def successAlm[T](x: T): AlmValidation[T] = x.success[Problem]
  
  def inTryCatch[T](a: => T, defaultProblemType: Problem = defaultProblem): AlmValidation[T] = {
    try {
      a.success[Problem]
    } catch  {
      case err => defaultProblemType.withMessage(err.getMessage).withException(err).fail[T]
    }
  }
  
  def computeSafely[T](a: => AlmValidation[T], defaultProblemType: Problem = defaultProblem): AlmValidation[T] = {
    try {
      a
    } catch {
      case err => defaultProblemType.withMessage(err.getMessage).withException(err).fail[T]
    }
  }

  def parseIntAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Int] =
    try {
      toParse.toInt.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Int):%s".format(toParse), key).fail[Int]
    }

  def parseLongAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Long] =
    try {
      toParse.toLong.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Long)".format(toParse), key).fail[Long]
    }
  
  def parseDoubleAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Double] =
    try {
      toParse.toDouble.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Double)".format(toParse), key).fail[Double]
    }

  def tryParseIntAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[Int]] =
    emptyStringIsNone(toParse, x => parseIntAlm(x, key))
 
  
  def tryParseLongAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[Long]] =
    emptyStringIsNone(toParse, x => parseLongAlm(x, key))
 
  
  def tryParseDoubleAlm(toParse: String, key: String = "some value"): AlmValidationSingleBadData[Option[Double]] =
    emptyStringIsNone(toParse, x => parseDoubleAlm(x, key))
 
  
  def notEmpty(toTest: String, key: String = "some value"): AlmValidationSingleBadData[String] =
    if(toTest.isEmpty) badData("must not be empty", key).fail[String] else toTest.success[SingleBadDataProblem]

  def notEmptyOrWhitespace(toTest: String, key: String = "some value"): AlmValidationSingleBadData[String] =
    if(toTest.trim.isEmpty) 
      badData("must not be empty or whitespaces", key).fail[String] 
    else 
      toTest.success[SingleBadDataProblem]
  
  def mustBeTrue(cond: => Boolean, problem: Problem): AlmValidation[Unit] =
    if(cond) ().successAlm else problem.fail[Unit]
  
  def noneIsBadData[T](v: Option[T], message: String = "No value supplied", key: String = "unknown"): AlmValidationSingleBadData[T] =
    v match {
      case Some(v) => v.success[SingleBadDataProblem]
      case None => SingleBadDataProblem(message, key = key).fail[T]
    }
  
  def noneIsNotFound[T](v: Option[T], message: String = "Not found"): AlmValidation[T] =
    v match {
      case Some(v) => v.success[NotFoundProblem]
      case None => NotFoundProblem(message).fail[T]
    }
  
  private def emptyStringIsNone[T](str: String, f: String => AlmValidationSingleBadData[T]) =
    if(str.trim.isEmpty)
      Success(None)
    else
      f(str).map(Some(_))
}

}