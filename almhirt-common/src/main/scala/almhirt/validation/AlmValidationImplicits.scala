package almhirt.validation

import scalaz.syntax.validation._
import scalaz.{Validation, ValidationNEL, Success, Failure, Semigroup}
import Problem._
import scalaz.Semigroup

trait AlmValidationImplicits {
  implicit def stringToStringW(str: String): StringW = new StringW(str)
  final class StringW(str: String) {
    def toIntAlm(key: String = "some value"): Validation[BadDataProblem, Int] = AlmValidation.parseIntAlm(str, key)
    def toLongAlm(key: String = "some value"): Validation[BadDataProblem, Long] =  AlmValidation.parseLongAlm(str, key)
    def toDoubleAlm(key: String = "some value"): Validation[BadDataProblem, Double] =  AlmValidation.parseDoubleAlm(str, key)
    def notEmptyAlm(key: String = "some value"): Validation[BadDataProblem, String] =  AlmValidation.failIfEmpty(str, key)
    def notEmptyOrWhitespaceAlm(key: String = "some value"): Validation[BadDataProblem, String] =  AlmValidation.failIfEmptyOrWhitespace(str, key)
  }

  implicit def any2AnyAlmW[T](x: T): AnyAlmW[T] = new AnyAlmW(x)
  final class AnyAlmW[T](any: T) {
    def successAlm(): AlmValidation[T] = any.success[Problem]  
  }

  def multipleBadDataFromValidationNel[T](validationNel: ValidationNEL[String, T]): AlmValidation[T] =
    validationNel match {
      case Success(r) => r.success[Problem]
      case Failure(nel) => 
        val keysAndMessages = nel.list.zipWithIndex.map{case (msg, i) => i.toString -> msg}
        MultipleBadDataProblem(
          "One or more errors found", 
          Map(keysAndMessages : _*)).fail[T]
      }

  
  implicit def validation2AlmValidationW[T](validation: Validation[String, T]) = new AlmValidationW[T](validation)
  final class AlmValidationNelW[T](validationNel: ValidationNEL[String, T]) {
    def failureIsMultipleBadData(): AlmValidation[T] = multipleBadDataFromValidationNel[T](validationNel)
  }

  implicit def validationNel2AlmValidationNelW[T](validationNel: ValidationNEL[String, T]) = new AlmValidationNelW[T](validationNel)
  final class AlmValidationW[T](validation: Validation[String, T]) {
    def toAlmValidation(problemOnFail: Problem = defaultProblem): AlmValidation[T] = {
      validation match {
        case Success(r) => r.success[Problem]
        case Failure(msg) => problemOnFail.withMessage(msg).fail[T]
      }
    }
  }

  implicit def badDataProblemValidationToBadDataProblemValidationW[T](badDataProblemValidation: Validation[BadDataProblem, T]) = 
    new BadDataProblemValidationW[T](badDataProblemValidation)
  final class BadDataProblemValidationW[T](badDataProblemValidation: Validation[BadDataProblem, T]) {
    def toMultipleBadData(): Validation[MultipleBadDataProblem, T] =
      badDataProblemValidation match {
      case Success(r) => r.success[MultipleBadDataProblem]
      case Failure(f) => f.toMultipleBadData().fail[T]
    }
  }
  
  
  implicit def fromValidationToValidationThrowableW[T](validation: Validation[Throwable, T]): ValidationThrowableW[T] =
    new ValidationThrowableW[T](validation)

  final class ValidationThrowableW[T](validation: Validation[Throwable, T]) {
    def fromExceptional(problemOnFail: Problem = defaultProblem): AlmValidation[T] = 
      validation match {
      case Success(r) => r.success[Problem]
      case Failure(exn) => problemOnFail.withMessage(exn.getMessage).withException(exn).fail[T] 
    }
  }
}