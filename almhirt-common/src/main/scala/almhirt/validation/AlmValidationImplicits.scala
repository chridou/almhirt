package almhirt.validation

import scalaz.syntax.validation._
import scalaz.{Validation, ValidationNEL, Success, Failure, Semigroup}
import Problem._
import scalaz.Semigroup

trait AlmValidationImplicits {
  implicit def stringToStringW(str: String): StringW = new StringW(str)
  final class StringW(str: String) {
    def toIntAlm(key: String = "some value"): AlmValidationSingleBadData[Int] = 
      AlmValidation.parseIntAlm(str, key)
    def toLongAlm(key: String = "some value"): AlmValidationSingleBadData[Long] =  
      AlmValidation.parseLongAlm(str, key)
    def toDoubleAlm(key: String = "some value"): AlmValidationSingleBadData[Double] =  
      AlmValidation.parseDoubleAlm(str, key)
    def notEmptyAlm(key: String = "some value"): AlmValidationSingleBadData[String] =  
      AlmValidation.notEmpty(str, key)
    def notEmptyOrWhitespaceAlm(key: String = "some value"): AlmValidationSingleBadData[String] =  
      AlmValidation.notEmptyOrWhitespace(str, key)
  }

  implicit def any2AnyAlmW[T](x: T): AnyAlmW[T] = new AnyAlmW(x)
  final class AnyAlmW[T](any: T) {
    def successAlm(): AlmValidation[T] = any.success[Problem]  
    def successSingleBadData(): AlmValidationSingleBadData[T] = any.success[SingleBadDataProblem]  
    def successMultipleBadData(): AlmValidationMultipleBadData[T] = any.success[MultipleBadDataProblem]  
  }

  def multipleBadDataFromValidationNel[T](validationNel: ValidationNEL[String, T]): AlmValidationMultipleBadData[T] =
    validationNel match {
      case Success(r) => r.successMultipleBadData
      case Failure(nel) => 
        val keysAndMessages = nel.list.zipWithIndex.map{case (msg, i) => "[i]".format(i) -> msg}
        MultipleBadDataProblem(
          "One or more errors found", 
          Map(keysAndMessages : _*)).fail[T]
      }

  
  implicit def validationNel2AlmValidationNelW[T](validationNel: ValidationNEL[String, T]) = new AlmValidationNelW[T](validationNel)
  final class AlmValidationNelW[T](validationNel: ValidationNEL[String, T]) {
    def toMultipleBadData(): Validation[MultipleBadDataProblem, T] = multipleBadDataFromValidationNel[T](validationNel)
  }

  implicit def validation2AlmValidationW[T](validation: Validation[String, T]) = new AlmValidationW[T](validation)
  final class AlmValidationW[T](validation: Validation[String, T]) {
    def toAlmValidation(problemOnFail: Problem = defaultProblem): AlmValidation[T] = {
      validation match {
        case Success(r) => r.success[Problem]
        case Failure(msg) => problemOnFail.withMessage(msg).fail[T]
      }
    }
  }

  implicit def option2AlmOptionW[T](opt: Option[T]) = new AlmOptionW(opt)
  final class AlmOptionW[T](opt: Option[T]) {
    def noneIsBadData(message: String = "No value supplied", key: String = "unknown"): AlmValidationSingleBadData[T] =
      AlmValidation.noneIsBadData(opt, message, key)
    def noneIsNotFound(message: String = "Not found"): AlmValidation[T] =
      AlmValidation.noneIsNotFound(opt, message)
  }
  
  implicit def badDataProblemValidationToSingleBadDataProblemValidationW[T](badDataProblemValidation: AlmValidationSingleBadData[T]) = 
    new SingleBadDataProblemValidationW[T](badDataProblemValidation)
  final class SingleBadDataProblemValidationW[T](badDataProblemValidation: AlmValidationSingleBadData[T]) {
    def toMultipleBadData(): AlmValidationMultipleBadData[T] =
      badDataProblemValidation match {
      case Success(r) => r.successMultipleBadData
      case Failure(f) => f.toMultipleBadData().fail[T]
    }
  }
  
  implicit def fromValidationToValidationThrowableW[T](validation: Validation[Throwable, T]): ValidationThrowableW[T] =
    new ValidationThrowableW[T](validation)
  final class ValidationThrowableW[T](validation: Validation[Throwable, T]) {
    def fromExceptional(problemOnFail: Problem = defaultProblem): AlmValidation[T] = 
      validation match {
      case Success(r) => r.successAlm
      case Failure(exn) => problemOnFail.withMessage(exn.getMessage).withException(exn).fail[T] 
    }
  }
}