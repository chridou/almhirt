package almhirt.validation

import java.util.UUID
import scalaz._, Scalaz._
import org.joda.time.DateTime
import Problem._


/** Implicits regarding [[almhirt.validation.AlmValidaion]] */
trait AlmValidationImplicits {
  
  implicit def stringToStringW(str: String): StringW = new StringW(str)
  /** Implicits for parsing Strings 
   *
   * Example:
   * {{{ 
   * val i = "5".toIntAlm
   * assert(i == 5.success)
   * }}}
   */
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
  
  /** Implicits for an option that contains a validation*/
  final class OptionValidationW[P, V](value: Option[Validation[P,V]]) {
    /** Option[Validation[P,T]] => Validation[P, Option[T]] */
    def validationOut(): Validation[P, Option[V]] =
      value match {
        case Some(validation) =>
          validation.fold(f => f.failure[Option[V]], _.success[P].map(Some(_)))
        case None => None.success[P]
    }
  }

  implicit def validationOption2OptionValidation[P, V](validation: Validation[P,Option[V]]): ValidationOptionW[P, V] =
    new ValidationOptionW(validation)
  /** Implicits for a validation that contains an option*/
  final class ValidationOptionW[P, V](validation: Validation[P,Option[V]]) {
    /** Validation[P, Option[T]]Option[Validation[P,T]] =>  */
    def optionOut(): Option[Validation[P,V]] =
      validation.fold(
          f => Some(f.failure[V]),
          s => s.map(_.success[P]))
  }
  
  implicit def validation2StringValidationW[T](validation: Validation[String, T]) = new StringValidationW[T](validation)
  final class StringValidationW[T](validation: Validation[String, T]) {
    def toAlmValidation(problemOnFail: Problem = defaultProblem): AlmValidation[T] =
      validation fold(problemOnFail.withMessage(_).failure[T], _.success[Problem])
  }

  implicit def validation2ValidationProblemW[P <: Problem, T](validation: Validation[P, T]) = new ValidationProblemW[P, T](validation)
  final class ValidationProblemW[P <: Problem, T](validation: Validation[P, T]) {
    def onFailure(sideEffect: Problem => Unit): Validation[P, T] = 
      validation fold (f => {sideEffect(f); validation}, _ => validation)
    
    def onSuccess(sideEffect: T => Unit): Validation[P, T] = 
      validation fold (_ => validation, r => {sideEffect(r); validation})
    
    def noProblem(v: => T): Validation[P, T] = 
      validation
        .fold(
            prob => if(prob.severity <= NoProblem) v.success[P] else validation,
            _ => validation)
 
    def compensate(v: => T): Validation[P, T] = 
      validation
        .fold(
            prob => if(prob.severity <= Minor) v.success[P] else validation,
            _ => validation)

    def recover(v: => T): Validation[P, T] = 
      validation
        .fold(
            prob => if(prob.severity <= Major) v.success[P] else validation,
            _ => validation)
    
    def forceResult(): T = 
      validation fold (prob => throw ValidationForcedException(prob), v => v)

    def toProblemOption(): Option[Problem] = 
      validation fold (prob => Some(prob), _ => None)
   }
  
  implicit def funOpt2AlmValidationW[T,U](f: T => Option[U]) = new FunOptAlmValidationW(f)
  final class FunOptAlmValidationW[T, U](f: T => Option[U]) {
    def >?(x:T): Validation[KeyNotFoundProblem, U] =
      f(x).map(_.success).getOrElse(KeyNotFoundProblem("Key not found: %s".format(x)).failure)
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
      badDataProblemValidation fold (_.toMBD().failure[T], _.success)
  }
  
  implicit def fromValidationToValidationThrowableW[T](validation: Validation[Throwable, T]): ValidationThrowableW[T] =
    new ValidationThrowableW[T](validation)
  final class ValidationThrowableW[T](validation: Validation[Throwable, T]) {
    def fromExceptional(problemOnFail: Problem = defaultProblem): AlmValidation[T] = 
      validation fold (exn => problemOnFail.withMessage(exn.getMessage).withException(exn).failure[T], _.success)
  }
  
  implicit def fromListValidation2ListAlmValidationW[R](v: List[AlmValidation[R]]): ListAlmValidationW[R] = new ListAlmValidationW(v)
  final class ListAlmValidationW[R](v: List[AlmValidation[R]]){
    def aggregateProblems(msg: String): AlmValidation[List[R]] = {
      v.partition(_.isSuccess) match {
        case (succs, Nil) => succs.flatMap(_.toOption).toList.success
        case (_, probs) => 
          val problems = probs.flatMap(_.toProblemOption)
          (NonEmptyList(problems.head, problems.tail: _*) aggregate (msg)).failure
      }
    }
    def aggregateProblems(): AlmValidation[List[R]] = aggregateProblems("One or more problems occured. See causes.")
  }
  
}