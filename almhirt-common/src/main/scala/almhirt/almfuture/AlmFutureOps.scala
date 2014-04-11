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
package almhirt.almfuture

import scala.language.implicitConversions
import scala.language.postfixOps

import scala.concurrent.{ Future, ExecutionContext }
import scala.reflect.ClassTag
import scalaz.Scalaz.ToValidationV
import scalaz.syntax.Ops
import almhirt.common._
import almhirt.almvalidation.kit._

/** Implicits on an untyped [[scala.concurrent.Future]] */
trait AlmFutureOps0 extends Ops[Future[Any]] {
  import scala.reflect._
  /**
   * Turn this [[scala.concurrent.Future]] into on [[almhirt.common.AlmFuture]] of the given type
   *
   * '''The real type of the [[scala.concurrent.Future]] must be [[almhirt.validation.AlmValidation[T]]]'''
   *
   * @tparam T The success type in [[almhirt.common.AlmValidation[T]]]
   */
  def mapToAlmFuture[T](implicit m: ClassTag[T]): AlmFuture[T] =
    new AlmFuture[T](self.mapTo[AlmValidation[T]])

  /** Same as [[almhirt.almfuture.AlmFutureOps0.mapToAlmFuture]] */
  def ~>[T](implicit m: ClassTag[T]): AlmFuture[T] = mapToAlmFuture
  /** Same as [[almhirt.almfuture.AlmFutureOps0.mapToAlmFuture]] */
  def ↝[T](implicit m: ClassTag[T]): AlmFuture[T] = mapToAlmFuture

  /**
   * Create a new [[almhirt.common.AlmFuture]] from a [[scala.concurrent.Future]] with T being a successful value
   *
   * '''The real type of the [[scala.concurrent.Future]] must NOT be [[almhirt.validation.AlmValidation]][_]'''
   *
   * @tparam T The successful result type of the Future
   */
  def successfulAlmFuture[T](implicit executionContext: ExecutionContext, t: scala.reflect.ClassTag[T]): AlmFuture[T] =
    new AlmFuture[T](self.mapTo[T].map(_.success)(executionContext))
}

trait AlmFutureOps1[T] extends Ops[Future[AlmValidation[T]]] {
  /**
   * Turn this [[scala.concurrent.Future]] into on [[almhirt.common.AlmFuture]] of the given type
   *
   * '''The real type of the [[scala.concurrent.Future]] must be [[almhirt.common.AlmValidation]][T]'''
   *
   * @tparam T The success type in the [[almhirt.common.AlmValidation]] of the successful Future of that [[almhirt.common.AlmValidation]]
   */
  def toAlmFuture: AlmFuture[T] =
    new AlmFuture[T](self)
}

/** Operations for starting a future operation from a [[almhirt.common.AlmValidation]] */
trait AlmFutureOps2[T] extends Ops[AlmValidation[T]] {
  /**
   * In case the [[almhirt.common.AlmValidation]] contains a value start the given computation otherwise return a failed [[almhirt.common.AlmFuture]]
   *
   * @param compute The function to execute async
   * @return The future async computation
   */
  def continueAsync[U](compute: T => AlmValidation[U])(implicit executionContext: ExecutionContext): AlmFuture[U] =
    self fold (
      prob => AlmFuture.failed(prob),
      r => AlmFuture { compute(r) })

  /** Same as [[almhirt.almfuture.AlmFutureOps2.continueAsync]] */
  def |~>[U](compute: T => AlmValidation[U])(implicit executionContext: ExecutionContext): AlmFuture[U] =
    continueAsync[U](compute)

  /**
   * In case the [[almhirt.common.AlmValidation]] contains a value execute the given side effect otherwise do nothing.
   *
   * @param action The side effect to execute async
   */
  def doAsync(action: T => Unit)(implicit executionContext: ExecutionContext) {
    self fold (
      prob => (),
      r => executionContext.execute(new Runnable { def run() = action(r) }))
  }

  /** Same as [[almhirt.almfuture.AlmFutureOps2.doAsync]] */
  def ~|(action: T => Unit)(implicit executionContext: ExecutionContext) {
    doAsync(action)
  }

  /**
   * Make this a completed [[almhirt.common.AlmFuture]]
   *
   * @param compute The computation
   */
  def asCompleted: AlmFuture[T] =
    self fold (
      prob => new AlmFuture(Future.successful(prob.failure)),
      r => AlmFuture.successful(r))

  /**
   * In case of a success: Start the given future
   *
   * @param compute The computation which eventually returns a result
   */
  def continueWithFuture[U](futureComputation: T => AlmFuture[U]): AlmFuture[U] =
    self fold (
      prob => new AlmFuture(Future.successful((prob.failure))),
      r => futureComputation(r))
}

trait AlmFutureOps3[T] extends Ops[Future[T]] {
  /**
   * Make the result a success for the returned [[almhirt.common.AlmFuture]]
   *
   * @param compute The computation which eventually returns a result
   */
  def toSuccessfulAlmFuture(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture[T](self.map(_.success)(executionContext))
}

trait AlmFutureOps4[T] extends Ops[AlmFuture[Option[T]]] {
  def noneIsProblem(problem: Problem)(implicit executionContext: ExecutionContext): AlmFuture[T] =
    self.collectV {
      case Some(x) => x.success
      case None => problem.failure
    }

  def noneIsNotFoundProblem(msg: String)(implicit executionContext: ExecutionContext): AlmFuture[T] =
    self.collectV {
      case Some(x) => x.success
      case None => NotFoundProblem(msg).failure
    }

  def noneIsMandatoryDataProblem(msg: String)(implicit executionContext: ExecutionContext): AlmFuture[T] =
    self.collectV {
      case Some(x) => x.success
      case None => MandatoryDataProblem(msg).failure
    }

  def noneIsNoSuchElementProblem(msg: String)(implicit executionContext: ExecutionContext): AlmFuture[T] =
    self.collectV {
      case Some(x) => x.success
      case None => NoSuchElementProblem(msg).failure
    }

}

trait AlmFutureOps5[T] extends Ops[AlmFuture[AlmFuture[T]]] {
  def flatten(implicit executionContext: ExecutionContext): AlmFuture[T] =
    self.flatMap(x => x)
}


trait ToAlmFutureOps {
  implicit def FromFutureToAlmFutureOps0(a: Future[Any]): AlmFutureOps0 = new AlmFutureOps0 { def self = a }
  implicit def FromTypedFutureToAlmFutureOps1[T](a: Future[AlmValidation[T]]): AlmFutureOps1[T] = new AlmFutureOps1[T] { def self = a }
  implicit def FromAlmValidationToAlmFutureOps2[T](a: AlmValidation[T]): AlmFutureOps2[T] = new AlmFutureOps2[T] { def self = a }
  implicit def FromTypedFutureToAlmFutureOps3[T](a: Future[T]): AlmFutureOps3[T] = new AlmFutureOps3[T] { def self = a }
  implicit def FromAlmFutureOptionToAlmFutureOps4[T](a: AlmFuture[Option[T]]): AlmFutureOps4[T] = new AlmFutureOps4[T] { def self = a }
  implicit def FromAlmFutureAlmFutureToAlmFutureOps5[T](a: AlmFuture[AlmFuture[T]]): AlmFutureOps5[T] = new AlmFutureOps5[T] { def self = a }
}