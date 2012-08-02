package almhirt.validation

import java.util.UUID
import scalaz.syntax.validation._
import scalaz.{Validation, ValidationNEL, Success, Failure, Semigroup}
import org.joda.time.DateTime
import Problem._

trait AlmValidationImplicits {
  implicit def stringToStringW(str: String): StringW = new StringW(str)
  final class StringW(str: String) {
    def toIntAlm(key: String = "some value"): AlmValidationSBD[Int] = 
      AlmValidation.parseIntAlm(str, key)
    def toLongAlm(key: String = "some value"): AlmValidationSBD[Long] =  
      AlmValidation.parseLongAlm(str, key)
    def toDoubleAlm(key: String = "some value"): AlmValidationSBD[Double] =  
      AlmValidation.parseDoubleAlm(str, key)
    def toFloatAlm(key: String = "some value"): AlmValidationSBD[Float] =  
      AlmValidation.parseFloatAlm(str, key)
    def toBooleanAlm(key: String = "some value"): AlmValidationSBD[Boolean] =  
      AlmValidation.parseBooleanAlm(str, key)
    def toDecimalAlm(key: String = "some value"): AlmValidationSBD[BigDecimal] =  
      AlmValidation.parseDecimalAlm(str, key)
    def toDateTimeAlm(key: String = "some value"): AlmValidationSBD[DateTime] =  
      AlmValidation.parseDateTimeAlm(str, key)
    def toUUIDAlm(key: String = "some value"): AlmValidationSBD[UUID] =  
      AlmValidation.parseUUIDAlm(str, key)
    def toBytesFromBase64Alm(key: String = "some value"): AlmValidationSBD[Array[Byte]] =  
      AlmValidation.parseBase64Alm(str, key)
    def notEmptyAlm(key: String = "some value"): AlmValidationSBD[String] =  
      AlmValidation.notEmpty(str, key)
    def notEmptyOrWhitespaceAlm(key: String = "some value"): AlmValidationSBD[String] =  
      AlmValidation.notEmptyOrWhitespace(str, key)
  }

  implicit def any2AnyAlmW[T](x: T): AnyAlmW[T] = new AnyAlmW(x)
  final class AnyAlmW[T](any: T) {
    def successAlm(): AlmValidation[T] = any.success[Problem]  
    def successSBD(): AlmValidationSBD[T] = any.success[SingleBadDataProblem]  
    def successMBD(): AlmValidationMBD[T] = any.success[MultipleBadDataProblem]  
    def successSM(): AlmValidationSM[T] = any.success[SingleMappingProblem]  
    def successMM(): AlmValidationMM[T] = any.success[MultipleMappingProblem]  
  }

  implicit def optionValidation2ValidationOption[P, V](value: Option[Validation[P,V]]): OptionValidationW[P, V] =
    new OptionValidationW(value)
  final class OptionValidationW[P, V](value: Option[Validation[P,V]]) {
    def validationOut(): Validation[P, Option[V]] =
      value match {
        case Some(validation) =>
          validation match {
            case Success(v) => v.success[P].map(Some(_))
            case Failure(f) => f.fail[Option[V]]
          }
        case None => None.success[P]
    }
  }

  implicit def validationOption2OptionValidation[P, V](value: Validation[P,Option[V]]): ValidationOptionW[P, V] =
    new ValidationOptionW(value)
  final class ValidationOptionW[P, V](value: Validation[P,Option[V]]) {
    def optionOut(): Option[Validation[P,V]] =
      value match {
        case Success(opt) =>
          opt match {
            case Some(v) => Some(v.success[P])
            case None => None
          }
        case Failure(f) => Some(f.fail[V])
    }
  }
  
  def multipleBadDataFromValidationNel[T](validationNel: ValidationNEL[String, T]): AlmValidationMBD[T] =
    validationNel match {
      case Success(r) => r.successMBD
      case Failure(nel) => 
        val keysAndMessages = nel.list.zipWithIndex.map{case (msg, i) => "[i]".format(i) -> msg}
        MultipleBadDataProblem(
          "One or more errors found", 
          Map(keysAndMessages : _*)).fail[T]
      }

  
  implicit def validationNel2AlmValidationNelW[T](validationNel: ValidationNEL[String, T]) = new AlmValidationNelW[T](validationNel)
  final class AlmValidationNelW[T](validationNel: ValidationNEL[String, T]) {
    def toMBD(): Validation[MultipleBadDataProblem, T] = multipleBadDataFromValidationNel[T](validationNel)
  }

  implicit def validation2StringValidationW[T](validation: Validation[String, T]) = new StringValidationW[T](validation)
  final class StringValidationW[T](validation: Validation[String, T]) {
    def toAlmValidation(problemOnFail: Problem = defaultProblem): AlmValidation[T] = {
      validation match {
        case Success(r) => r.success[Problem]
        case Failure(msg) => problemOnFail.withMessage(msg).fail[T]
      }
    }
  }

  implicit def validation2ValidationProblemW[P <: Problem, T](validation: Validation[P, T]) = new ValidationProblemW[P, T](validation)
  final class ValidationProblemW[P <: Problem, T](validation: Validation[P, T]) {
    def onFailure(sideEffect: Problem => Unit): Validation[P, T] = {
      validation match {
        case Success(_) => 
          validation
        case Failure(f) =>
          sideEffect(f)
          validation
      }
    }
    
    def onSuccess(sideEffect: T => Unit): Validation[P, T] = {
      validation match {
        case Success(r) => 
          sideEffect(r)
          validation
        case Failure(f) =>
          validation
      }
    }
    
    def noProblem(v: => T): Validation[P, T] = 
      validation match {
        case Success(_) => validation
        case Failure(prob) =>
          if(prob.severity <= NoProblem)
            v.success[P]
          else
            validation
      }

    def compensate(v: => T): Validation[P, T] = 
      validation match {
        case Success(_) => validation
        case Failure(prob) =>
          if(prob.severity <= Minor)
            v.success[P]
          else
            validation
      }

    def recover(v: => T): Validation[P, T] = 
      validation match {
        case Success(_) => validation
        case Failure(prob) =>
          if(prob.severity <= Major)
            v.success[P]
          else
            validation
      }
    
    def forceResult(): T = {
      validation match {
        case Success(v) => v
        case Failure(prob) => throw ValidationException(prob)
      }
    }

//    def escalate(severity: => Severity): Validation[P, T] = 
//      validation match {
//        case Success(_) => validation
//        case Failure(prob) =>
//          if(prob.severity < severity)
//            prob.withSeverity(severity).fail[T]
//          else
//            validation
//    }
  }
  
  
  implicit def option2AlmOptionW[T](opt: Option[T]) = new AlmOptionW(opt)
  final class AlmOptionW[T](opt: Option[T]) {
    def noneIsBadData(message: String = "No value supplied", key: String = "unknown"): AlmValidationSBD[T] =
      AlmValidation.noneIsBadData(opt, message, key)
    def noneIsNotFound(message: String = "Not found"): AlmValidation[T] =
      AlmValidation.noneIsNotFound(opt, message)
  }
  
  implicit def badDataProblemValidationToSingleBadDataProblemValidationW[T](badDataProblemValidation: AlmValidationSBD[T]) = 
    new SingleBadDataProblemValidationW[T](badDataProblemValidation)
  final class SingleBadDataProblemValidationW[T](badDataProblemValidation: AlmValidationSBD[T]) {
    def toMBD(): AlmValidationMBD[T] =
      badDataProblemValidation match {
      case Success(r) => r.successMBD
      case Failure(f) => f.toMBD().fail[T]
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