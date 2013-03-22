/* Copyright 2012 Christian Douven

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package almhirt.almvalidation

import scala.language.implicitConversions

import scala.reflect.ClassTag
import java.util.UUID
import scalaz.{ Validation, NonEmptyList }
import scalaz.syntax.Ops
import scalaz.syntax.validation._
import org.joda.time.DateTime
import almhirt.common._

/**
 * Implicits for parsing Strings
 *
 * Example:
 * {{{
 * val i = "5".toIntAlm
 * assert(i == 5.success)
 * }}}
 */
trait AlmValidationOps0 extends Ops[String] {
  import funs._
  def toBooleanAlm(): AlmValidation[Boolean] =
    parseBooleanAlm(self)
  def toByteAlm(): AlmValidation[Byte] =
    parseByteAlm(self)
  def toIntAlm(): AlmValidation[Int] =
    parseIntAlm(self)
  def toLongAlm(): AlmValidation[Long] =
    parseLongAlm(self)
  def toBigIntAlm(): AlmValidation[BigInt] =
    parseBigIntAlm(self)
  def toDoubleAlm(): AlmValidation[Double] =
    parseDoubleAlm(self)
  def toFloatAlm(): AlmValidation[Float] =
    parseFloatAlm(self)
  def toDecimalAlm(): AlmValidation[BigDecimal] =
    parseDecimalAlm(self)
  def toDateTimeAlm(): AlmValidation[DateTime] =
    parseDateTimeAlm(self)
  def toUuidAlm(): AlmValidation[UUID] =
    parseUuidAlm(self)
  def toUriAlm(): AlmValidation[java.net.URI] =
    parseUriAlm(self)
  def notEmptyAlm(): AlmValidation[String] =
    notEmpty(self)


}

trait AlmValidationOps1[T] extends Ops[T] {
  def successAlm(): AlmValidation[T] = self.success[Problem]
}

/** Implicits for an option that contains a validation*/
trait AlmValidationOps2[P, V] extends Ops[Option[Validation[P, V]]] {
  /** Option[Validation[P,T]] => Validation[P, Option[T]] */
  def validationOut(): Validation[P, Option[V]] =
    self match {
      case Some(validation) =>
        validation.fold(f => f.failure[Option[V]], _.success[P].map(Some(_)))
      case None => None.success[P]
    }
}

/** Implicits for a validation that contains an option*/
trait AlmValidationOps3[P, V] extends Ops[Validation[P, Option[V]]] {
  /** Validation[P, Option[T]]Option[Validation[P,T]] =>  */
  def optionOut(): Option[Validation[P, V]] =
    self.fold(
      f => Some(f.failure[V]),
      s => s.map(_.success[P]))
}

trait AlmValidationOps4[T] extends Ops[Validation[String, T]] {
  def toAlmValidation(toProblem: String => Problem): AlmValidation[T] =
    self fold (msg => toProblem(msg).failure[T], _.success[Problem])
}

trait AlmValidationOps5[P <: Problem, T] extends Ops[Validation[P, T]] {
  def onFailure(sideEffect: P => Unit): Unit =
    self fold (sideEffect, _ => ())

  def onSuccess(sideEffect: T => Unit): Unit =
    self fold (_ => (), sideEffect)

  def onResult(failEffect: P => Unit, sucessEffect: T => Unit): Unit =
    self fold (failEffect, sucessEffect)

  def withFailEffect(failEffect: P => Unit): Validation[P, T] =
    self fold (p => { failEffect(p); self }, _ => self)

  def noProblem(v: => T): Validation[P, T] =
    self
      .fold(
        prob => if (prob.severity <= NoProblem) v.success[P] else self,
        _ => self)

  def compensate(v: => T): Validation[P, T] =
    self
      .fold(
        prob => if (prob.severity <= Minor) v.success[P] else self,
        _ => self)

  def recover(v: => T): Validation[P, T] =
    self
      .fold(
        prob => if (prob.severity <= Major) v.success[P] else self,
        _ => self)

  /** Never use in production code! */
  def forceResult(): T =
    self fold (prob => throw ResultForcedFromValidationException(prob), v => v)

  /** Never use in production code! */
  def forceProblem(): P =
    self fold (prob => prob, v => throw new ProblemForcedFromValidationException())

  def toProblemOption(): Option[Problem] =
    self fold (prob => Some(prob), _ => None)

  def sideEffect(f: P => Unit, s: T => Unit): Unit =
    self fold (prob => f(prob), t => s(t))
}

trait AlmValidationOps6[T, U] extends Ops[T => Option[U]] {
  def >!(x: T): Validation[KeyNotFoundProblem, U] =
    self(x).map(_.success).getOrElse(KeyNotFoundProblem("Key not found: %s".format(x)).failure)
}

trait AlmValidationOps7[T] extends Ops[Option[T]] {
  def noneIsBadData(): AlmValidation[T] =
    funs.noneIsBadData(self)
  def noneIsNotFound(): AlmValidation[T] =
    funs.noneIsNotFound(self)
  def noneIsNoSuchElement(): AlmValidation[T] =
    funs.noneIsNoSuchElement(self)
}

trait AlmValidationOps9[T] extends Ops[AlmValidation[T]] {
  def toAgg(msg: String): AlmValidationAP[T] =
    self fold (
      prob =>
        if (prob.isInstanceOf[AggregateProblem])
          prob.asInstanceOf[AggregateProblem].failure
        else
          AggregateProblem(msg, severity = prob.severity, category = prob.category, problems = List(prob)).failure,
      _.success)

  def toAgg(): AlmValidationAP[T] =
    toAgg("One or more problems occured. See problems.")

  def invert(): scalaz.Validation[T, Problem] =
    self fold (fail => fail.success, succ => succ.failure)

  def withArgOnFailure(key: String, value: Any): AlmValidation[T] = self.bimap(p => p.withArg(key, value), g => g)
  def withIdentifierOnFailure(ident: String): AlmValidation[T] = self.bimap(p => almhirt.problem.funs.withIdentifier(p, ident), g => g)
}

trait AlmValidationOps10[T] extends Ops[Validation[Throwable, T]] {
  def fromExceptional(): AlmValidation[T] =
    self fold (exn => ExceptionCaughtProblem(exn.getMessage, cause = Some(exn)).failure[T], _.success)
}

trait AlmValidationOps11[T] extends Ops[Either[Throwable, T]] {
  def toAlmValidation(): AlmValidation[T] =
    self fold (exn => ExceptionCaughtProblem(exn.getMessage, cause = Some(exn)).failure[T], _.success)
}

trait AlmValidationOps12[R] extends Ops[List[AlmValidation[R]]] {
  import almhirt.syntax.problem._
  import almhirt.syntax.almvalidation._
  def aggregateProblems(msg: String): Validation[AggregateProblem, List[R]] = {
    self.partition(_.isSuccess) match {
      case (succs, Nil) => succs.flatMap(_.toOption).toList.success
      case (_, probs) =>
        val problems = probs.flatMap(_.toProblemOption)
        (NonEmptyList(problems.head, problems.tail: _*) aggregate (msg)).failure
    }
  }

  def aggregateProblems(): Validation[AggregateProblem, List[R]] = aggregateProblems("One or more problems occured. See problems.")
}

trait AlmValidationOps13 extends Ops[Any] {
  def castTo[To](implicit tag: ClassTag[To]): AlmValidation[To] = almhirt.almvalidation.funs.almCast[To](self)
}

trait ToAlmValidationOps {
  implicit def FromStringToAlmValidationOps0(a: String): AlmValidationOps0 = new AlmValidationOps0 { def self = a }
  implicit def FromAnyToAlmValidationOps1[T](a: T): AlmValidationOps1[T] = new AlmValidationOps1[T] { def self = a }
  implicit def FromOptionValidationToAlmValidationOps2[P, V](a: Option[Validation[P, V]]): AlmValidationOps2[P, V] = new AlmValidationOps2[P, V] { def self = a }
  implicit def FromValidationOptionToAlmValidationOps3[P, V](a: Validation[P, Option[V]]): AlmValidationOps3[P, V] = new AlmValidationOps3[P, V] { def self = a }
  implicit def FromValidationStringTToAlmValidationOps4[T](a: Validation[String, T]): AlmValidationOps4[T] = new AlmValidationOps4[T] { def self = a }
  implicit def FromvalidationProblemToAlmValidationOps5[P <: Problem, T](a: Validation[P, T]): AlmValidationOps5[P, T] = new AlmValidationOps5[P, T] { def self = a }
  implicit def FromFunOptToAlmValidationOps6[T, U](a: T => Option[U]): AlmValidationOps6[T, U] = new AlmValidationOps6[T, U] { def self = a }
  implicit def FromOptionTOAlmAlmValidationOps7[T](a: Option[T]): AlmValidationOps7[T] = new AlmValidationOps7[T] { def self = a }
  //  implicit def FromBadDataProblemValidationToAlmValidationOps8[T](a: AlmValidationSBD[T]): AlmValidationOps8[T] = new AlmValidationOps8[T] { def self = a }
  implicit def FromAlmValidationToAlmValidationOps9[T](a: AlmValidation[T]): AlmValidationOps9[T] = new AlmValidationOps9[T] { def self = a }
  implicit def FromValidationToAlmValidationOps10[T](a: Validation[Throwable, T]): AlmValidationOps10[T] = new AlmValidationOps10[T] { def self = a }
  implicit def FromEitherThrowableToAlmValidationOps11[T](a: Either[Throwable, T]): AlmValidationOps11[T] = new AlmValidationOps11[T] { def self = a }
  implicit def FromListValidationToAlmValidationOps12[R](a: List[AlmValidation[R]]): AlmValidationOps12[R] = new AlmValidationOps12[R] { def self = a }
  implicit def FromAnyToAlmValidationOps13(a: Any): AlmValidationOps13 = new AlmValidationOps13 { def self = a }
}