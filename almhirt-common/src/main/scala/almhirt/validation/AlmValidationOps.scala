package almhirt.validation
/** Implicits regarding [[almhirt.validation.AlmValidaion]] */
package syntax

import java.util.UUID
import scalaz.{Validation, NonEmptyList}
import scalaz.syntax.Ops
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.validation._

/** Implicits for parsing Strings 
 *
 * Example:
 * {{{ 
 * val i = "5".toIntAlm
 * assert(i == 5.success)
 * }}}
 */
trait AlmValidationOps0 extends Ops[String] {
  import AlmValidationFunctions._
  def toIntAlm(key: String = "some value"): AlmValidationSBD[Int] = 
    parseIntAlm(self, key)
  def toLongAlm(key: String = "some value"): AlmValidationSBD[Long] =  
    parseLongAlm(self, key)
  def toDoubleAlm(key: String = "some value"): AlmValidationSBD[Double] =  
    parseDoubleAlm(self, key)
  def toFloatAlm(key: String = "some value"): AlmValidationSBD[Float] =  
    parseFloatAlm(self, key)
  def toBooleanAlm(key: String = "some value"): AlmValidationSBD[Boolean] =  
    parseBooleanAlm(self, key)
  def toDecimalAlm(key: String = "some value"): AlmValidationSBD[BigDecimal] =  
    parseDecimalAlm(self, key)
  def toDateTimeAlm(key: String = "some value"): AlmValidationSBD[DateTime] =  
    parseDateTimeAlm(self, key)
  def toUUIDAlm(key: String = "some value"): AlmValidationSBD[UUID] =  
    parseUUIDAlm(self, key)
  def toBytesFromBase64Alm(key: String = "some value"): AlmValidationSBD[Array[Byte]] =  
    parseBase64Alm(self, key)
  def notEmptyAlm(key: String = "some value"): AlmValidationSBD[String] =  
    notEmpty(self, key)
  def notEmptyOrWhitespaceAlm(key: String = "some value"): AlmValidationSBD[String] =  
    notEmptyOrWhitespace(self, key)
}

trait AlmValidationOps1[T] extends Ops[T] {
  def successAlm(): AlmValidation[T] = self.success[Problem]  
  def successSBD(): AlmValidationSBD[T] = self.success[SingleBadDataProblem]  
  def successMBD(): AlmValidationMBD[T] = self.success[MultipleBadDataProblem]  
  def successSM(): AlmValidationSM[T] = self.success[SingleMappingProblem]  
  def successMM(): AlmValidationMM[T] = self.success[MultipleMappingProblem]  
}
  
  
/** Implicits for an option that contains a validation*/
trait AlmValidationOps2[P, V]extends Ops[Option[Validation[P,V]]] {
  /** Option[Validation[P,T]] => Validation[P, Option[T]] */
  def validationOut(): Validation[P, Option[V]] =
    self match {
      case Some(validation) =>
        validation.fold(f => f.failure[Option[V]], _.success[P].map(Some(_)))
      case None => None.success[P]
  }
}

/** Implicits for a validation that contains an option*/
trait AlmValidationOps3[P, V] extends Ops[Validation[P,Option[V]]] {
    /** Validation[P, Option[T]]Option[Validation[P,T]] =>  */
  def optionOut(): Option[Validation[P,V]] =
    self.fold(
      f => Some(f.failure[V]),
     s => s.map(_.success[P]))
}
  
trait AlmValidationOps4[T] extends Ops[Validation[String, T]] {
  import ProblemDefaults._
  def toAlmValidation(problemOnFail: Problem = defaultProblem): AlmValidation[T] =
    self fold(problemOnFail.withMessage(_).failure[T], _.success[Problem])
}

trait AlmValidationOps5[P <: Problem, T] extends Ops[Validation[P, T]] {
  def onFailure(sideEffect: Problem => Unit): Validation[P, T] = 
    self fold (f => {sideEffect(f); self}, _ => self)
    
  def onSuccess(sideEffect: T => Unit): Validation[P, T] = 
    self fold (_ => self, r => {sideEffect(r); self})
   
  def noProblem(v: => T): Validation[P, T] = 
    self
      .fold(
          prob => if(prob.severity <= NoProblem) v.success[P] else self,
          _ => self)
 
  def compensate(v: => T): Validation[P, T] = 
    self
      .fold(
        prob => if(prob.severity <= Minor) v.success[P] else self,
        _ => self)

  def recover(v: => T): Validation[P, T] = 
    self
      .fold(
        prob => if(prob.severity <= Major) v.success[P] else self,
        _ => self)
    
  def forceResult(): T = 
    self fold (prob => throw ValidationForcedException(prob), v => v)

  def toProblemOption(): Option[Problem] = 
    self fold (prob => Some(prob), _ => None)
}
  
trait AlmValidationOps6[T, U] extends Ops[T => Option[U]] {
  def >?(x:T): Validation[KeyNotFoundProblem, U] =
    self(x).map(_.success).getOrElse(KeyNotFoundProblem("Key not found: %s".format(x)).failure)
}
  
trait AlmValidationOps7[T] extends Ops[Option[T]] {
  def noneIsBadData(message: String = "No value supplied", key: String = "unknown"): AlmValidationSBD[T] =
    AlmValidationFunctions.noneIsBadData(self, message, key)
  def noneIsNotFound(message: String = "Not found"): AlmValidation[T] =
    AlmValidationFunctions.noneIsNotFound(self, message)
}
  
trait AlmValidationOps8[T] extends Ops[AlmValidationSBD[T]] {
  def toMBD(): AlmValidationMBD[T] =
    self fold (_.toMBD().failure[T], _.success)
}
  
trait AlmValidationOps9[T] extends Ops[Validation[Throwable, T]] {
  import ProblemDefaults._
  def fromExceptional(problemOnFail: Problem = defaultProblem): AlmValidation[T] = 
    self fold (exn => problemOnFail.withMessage(exn.getMessage).withException(exn).failure[T], _.success)
}
  
trait AlmValidationOps10[R] extends Ops[List[AlmValidation[R]]] {
  import ProblemOps._
  def aggregateProblems(msg: String): Validation[AggregateProblem, List[R]] = {
    import AlmValidationOps._
    self.partition(_.isSuccess) match {
      case (succs, Nil) => succs.flatMap(_.toOption).toList.success
      case (_, probs) => 
        val problems = probs.flatMap(_.toProblemOption)
        (NonEmptyList(problems.head, problems.tail: _*) aggregate (msg)).failure
    }
  }
  
  def aggregateProblems(): Validation[AggregateProblem, List[R]] = aggregateProblems("One or more problems occured. See causes.")
}

trait ToAlmValidationOps {
  implicit def FromStringToAlmValidationOps0(a: String): AlmValidationOps0 = new AlmValidationOps0{ def self = a }
  implicit def FromAnyToAlmValidationOps1[T](a: T): AlmValidationOps1[T] = new AlmValidationOps1[T]{ def self = a }
  implicit def FromOptionValidationToAlmValidationOps2[P, V](a: Option[Validation[P,V]]): AlmValidationOps2[P, V] = new AlmValidationOps2[P,V] { def self = a }
  implicit def FromValidationOptionToAlmValidationOps3[P, V](a: Validation[P,Option[V]]): AlmValidationOps3[P, V] = new AlmValidationOps3[P,V] { def self = a }
  implicit def FromValidationStringTToAlmValidationOps4[T](a: Validation[String, T]): AlmValidationOps4[T] = new AlmValidationOps4[T] { def self = a }
  implicit def FromvalidationProblemToAlmValidationOps5[P <: Problem, T](a: Validation[P, T]): AlmValidationOps5[P, T] = new AlmValidationOps5[P, T] { def self = a }
  implicit def FromFunOptToAlmValidationOps6[T,U](a: T => Option[U]): AlmValidationOps6[T,U]  = new AlmValidationOps6[T,U] { def self = a }
  implicit def FromOptionTOAlmAlmValidationOps7[T](a: Option[T]): AlmValidationOps7[T]= new AlmValidationOps7[T]{ def self = a }
  implicit def FromBadDataProblemValidationToAlmValidationOps8[T](a: AlmValidationSBD[T]): AlmValidationOps8[T] = new AlmValidationOps8[T] { def self = a }
  implicit def FromValidationToAlmValidationOps9[T](a: Validation[Throwable, T]): AlmValidationOps9[T] = new AlmValidationOps9[T]{ def self = a }
  implicit def FromListValidationToAlmValidationOps10[R](a: List[AlmValidation[R]]): AlmValidationOps10[R] = new AlmValidationOps10[R]{ def self = a }
}

object AlmValidationOps extends ToAlmValidationOps