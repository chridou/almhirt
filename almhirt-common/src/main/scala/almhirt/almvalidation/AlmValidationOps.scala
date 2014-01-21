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
import org.joda.time.{ DateTime, LocalDateTime }
import almhirt.common._
import scala.collection.TraversableLike
import scala.collection.generic.CanBuildFrom

import scala.language.higherKinds
import scala.collection.GenTraversableLike

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
  def toShortAlm(): AlmValidation[Short] =
    parseShortAlm(self)
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
  def toLocalDateTimeAlm(): AlmValidation[LocalDateTime] =
    parseLocalDateTimeAlm(self)
  def toDurationAlm(): AlmValidation[scala.concurrent.duration.FiniteDuration] =
    parseDurationAlm(self)
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
  def failureEffect(sideEffect: P => Unit): Unit =
    self fold (sideEffect, _ => ())

  def successEffect(sideEffect: T => Unit): Unit =
    self fold (_ => (), sideEffect)

  def effect(failEffect: P => Unit, sucessEffect: T => Unit): Unit =
    self fold (failEffect, sucessEffect)

  def recover(v: => T): Validation[P, T] =
    self.fold(prob => v.success[P], _ => self)

  /** Never use in production code! */
  def forceResult(): T =
    self fold (prob => throw ResultForcedFromValidationException(prob), v => v)

  /** Never use in production code! */
  def forceProblem(): P =
    self fold (prob => prob, v => throw new ProblemForcedFromValidationException())

  /**
   *  Escalate a problem.
   *  Call if you need a result and you don't no how to recover from a failure.
   *  A failure will throw an [[almhirt.common.EscalatedProblemException]] exception.
   */
  def resultOrEscalate(): T =
    self fold (prob => throw new EscalatedProblemException(prob), v => v)

  /** Returns a problem in a Some */
  def toProblemOption(): Option[Problem] =
    self fold (prob => Some(prob), _ => None)
}

trait AlmValidationOps6[T, U] extends Ops[T => Option[U]] {
  /** If the result is None return a  NoSuchElementProblem */
  def >!(x: T): AlmValidation[U] =
    self(x).map(_.success).getOrElse(NoSuchElementProblem(s"Key not found: ${x.toString}".format(x)).failure)
}

trait AlmValidationOps6A[A, B] extends Ops[Map[A, B]] {
  /** Get the value from the map or contain a NoSuchElementProblem */
  def getV(key: A): AlmValidation[B] = funs.getFromMap(key, self)

  /** Get the value from the map or contain a NoSuchElementProblem */
  def >!(key: A): AlmValidation[B] = funs.getFromMap(key, self)
}

trait AlmValidationOps7[T] extends Ops[Option[T]] {
  def mandatory(): AlmValidation[T] =
    funs.argumentIsMandatory(self)

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
          MultipleProblems(List(prob)).failure,
      _.success)

  def toAgg(): AlmValidationAP[T] =
    toAgg("One or more problems occured. See problems.")

  def invert(): scalaz.Validation[T, Problem] =
    self fold (fail => fail.success, succ => succ.failure)
}

trait AlmValidationOps10[T] extends Ops[Validation[Throwable, T]] {
  def fromExceptional(): AlmValidation[T] =
    self fold (exn => ExceptionCaughtProblem(exn).failure[T], _.success)
}

trait AlmValidationOps11[T] extends Ops[Either[Throwable, T]] {
  def toAlmValidation(): AlmValidation[T] =
    self fold (exn => ExceptionCaughtProblem(exn).failure[T], _.success)
}

//trait AlmValidationOps12A[R] extends Ops[List[AlmValidation[R]]] {
//  import almhirt.syntax.problem._
//  import almhirt.syntax.almvalidation._
//
//  /** Aggregates all Problems into a single AggregateProblem or contains the results  */
//  def aggregateProblems: Validation[AggregateProblem, List[R]] = {
//    self.partition(_.isSuccess) match {
//      case (succs, Nil) => succs.flatMap(_.toOption).success
//      case (_, probs) =>
//        val problems = probs.flatMap(_.toProblemOption)
//        (NonEmptyList(problems.head, problems.tail: _*).aggregate).failure
//    }
//  }
//}

trait AlmValidationOps12B[R, M[_] <: Traversable[_]] extends Ops[M[AlmValidation[R]]] {
  import almhirt.syntax.problem._
  import almhirt.syntax.almvalidation._

  /** Aggregates all Problems into a single AggregateProblem or contains the results  */
  def aggregateProblems(implicit cbf: CanBuildFrom[M[R], R, M[R]]): Validation[AggregateProblem, M[R]] = {
    val builderR = cbf()
    val probs = scala.collection.mutable.ListBuffer[Problem]()
    self.asInstanceOf[Traversable[AlmValidation[R]]].foreach {
      case scalaz.Success(x) => builderR += x
      case scalaz.Failure(x) => probs += x
    }
    if (probs.isEmpty)
      builderR.result.success
    else
      almhirt.problem.AggregateProblem(probs).failure
  }

  def splitValidations(implicit cbfR: CanBuildFrom[M[R], R, M[R]], cbfP: CanBuildFrom[M[Problem], Problem, M[Problem]]): (M[Problem], M[R]) = {
    val builderR = cbfR()
    val builderP = cbfP()
    self.asInstanceOf[Traversable[AlmValidation[R]]].foreach {
      case scalaz.Success(x) => builderR += x
      case scalaz.Failure(x) => builderP += x
    }
    (builderP.result, builderR.result)
  }

}

trait AlmValidationOps13 extends Ops[Any] {
  def castTo[To](implicit tag: ClassTag[To]): AlmValidation[To] = almhirt.almvalidation.funs.almCast[To](self)
}

trait AlmValidationOps14 extends Ops[AlmValidation[Boolean]] {
  /** Does this Boolean validation a value that equals true ? */
  def explicitlyTrue = self fold (_ => false, x => true == x)

  /** Does this Boolean validation a value that equals false ? */
  def explicitlyFalse = self fold (_ => false, x => false == x)

  /** Does this Boolean validation a value that equals false or is a failure ? */
  def falseOrFailure = self fold (_ => true, x => false == x)
}

trait AlmValidationOps15[T] extends Ops[scala.util.Try[T]] {
  def toValidation(): AlmValidation[T] =
    self match {
      case scala.util.Success(x) => x.success
      case scala.util.Failure(exn) => ExceptionCaughtProblem(exn).failure[T]
    }
}

trait ToAlmValidationOps {
  implicit def FromStringToAlmValidationOps0(a: String): AlmValidationOps0 = new AlmValidationOps0 { def self = a }
  implicit def FromAnyToAlmValidationOps1[T](a: T): AlmValidationOps1[T] = new AlmValidationOps1[T] { def self = a }
  implicit def FromOptionValidationToAlmValidationOps2[P, V](a: Option[Validation[P, V]]): AlmValidationOps2[P, V] = new AlmValidationOps2[P, V] { def self = a }
  implicit def FromValidationOptionToAlmValidationOps3[P, V](a: Validation[P, Option[V]]): AlmValidationOps3[P, V] = new AlmValidationOps3[P, V] { def self = a }
  implicit def FromValidationStringTToAlmValidationOps4[T](a: Validation[String, T]): AlmValidationOps4[T] = new AlmValidationOps4[T] { def self = a }
  implicit def FromvalidationProblemToAlmValidationOps5[P <: Problem, T](a: Validation[P, T]): AlmValidationOps5[P, T] = new AlmValidationOps5[P, T] { def self = a }
  implicit def FromFunOptToAlmValidationOps6[T, U](a: T => Option[U]): AlmValidationOps6[T, U] = new AlmValidationOps6[T, U] { def self = a }
  implicit def FromFunOptToAlmValidationOps6A[A, B](a: Map[A, B]): AlmValidationOps6A[A, B] = new AlmValidationOps6A[A, B] { def self = a }
  implicit def FromOptionTOAlmAlmValidationOps7[T](a: Option[T]): AlmValidationOps7[T] = new AlmValidationOps7[T] { def self = a }
  implicit def FromAlmValidationToAlmValidationOps9[T](a: AlmValidation[T]): AlmValidationOps9[T] = new AlmValidationOps9[T] { def self = a }
  implicit def FromValidationToAlmValidationOps10[T](a: Validation[Throwable, T]): AlmValidationOps10[T] = new AlmValidationOps10[T] { def self = a }
  implicit def FromEitherThrowableToAlmValidationOps11[T](a: Either[Throwable, T]): AlmValidationOps11[T] = new AlmValidationOps11[T] { def self = a }
 // implicit def FromListValidationToAlmValidationOps12A[R](a: List[AlmValidation[R]]): AlmValidationOps12A[R] = new AlmValidationOps12A[R] { def self = a }
  implicit def FromListValidationToAlmValidationOps12B[R, M[_] <: Traversable[_]](a: M[AlmValidation[R]]): AlmValidationOps12B[R, M] = new AlmValidationOps12B[R, M] { def self = a }
  implicit def FromAnyToAlmValidationOps13(a: Any): AlmValidationOps13 = new AlmValidationOps13 { def self = a }
  implicit def FromAnyToAlmValidationOps14(a: AlmValidation[Boolean]): AlmValidationOps14 = new AlmValidationOps14 { def self = a }
  implicit def FromAnyToAlmValidationOps15[T](a: scala.util.Try[T]): AlmValidationOps15[T] = new AlmValidationOps15[T] { def self = a }
}