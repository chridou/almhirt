package almhirt

import scalaz.{Validation, Success, Failure, ValidationNEL}
import scalaz.syntax.validation._

package object validation {
  type AlmValidation[+α] = ({type λ[α] = Validation[Problem, α]})#λ[α]

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

  def parseIntAlm(toParse: String, key: String = "some value"): Validation[SingleBadDataProblem, Int] =
    try {
      toParse.toInt.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Int):%s".format(toParse), key).fail[Int]
    }
  
  def parseLongAlm(toParse: String, key: String = "some value"): Validation[SingleBadDataProblem, Long] =
    try {
      toParse.toLong.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Long)".format(toParse), key).fail[Long]
    }
  
  def parseDoubleAlm(toParse: String, key: String = "some value"): Validation[SingleBadDataProblem, Double] =
    try {
      toParse.toDouble.success[SingleBadDataProblem]
    } catch {
      case err => badData("Not a valid number(Double)".format(toParse), key).fail[Double]
    }
  
  def failIfEmpty(toTest: String, key: String = "some value"): Validation[SingleBadDataProblem, String] =
    if(toTest.isEmpty) badData("must not be empty", key).fail[String] else toTest.success[SingleBadDataProblem]

  def failIfEmptyOrWhitespace(toTest: String, key: String = "some value"): Validation[SingleBadDataProblem, String] =
    if(toTest.trim.isEmpty) 
      badData("must not be empty or whitespaces", key).fail[String] 
    else 
      toTest.success[SingleBadDataProblem]
  
  def failIfFalse(cond: => Boolean, problem: Problem): AlmValidation[Unit] =
    if(cond) ().successAlm else problem.fail[Unit]
  
  def noneIsBadData[T](v: Option[T], message: String = "No value supplied", key: String = "unknown"): Validation[SingleBadDataProblem, T] =
    v match {
      case Some(v) => v.success[SingleBadDataProblem]
      case None => SingleBadDataProblem(message, key = key).fail[T]
    }
  
  def noneIsNotFound[T](v: Option[T], message: String = "Not found"): AlmValidation[T] =
    v match {
      case Some(v) => v.success[NotFoundProblem]
      case None => NotFoundProblem(message).fail[T]
    }
  
}

}