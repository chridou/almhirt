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
package almhirt.core

import java.util.concurrent.TimeoutException
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scalaz.syntax.validation._
import akka.dispatch.{ Future, Promise, Await, ExecutionContext }
import almhirt.common._
import akka.util.Duration

/**
 * A future based on [[akka.dispatch.Future]].
 *
 * The intention is to have a future that doesn't rely on the Either type where Left[Throwable] identifies an error.
 * Instead a result should always be in a [[almhirt.validation.AlmValidation]] which is in fact a [[scalaz.Validation]]
 * based on [[almhirt.validation.Problem]] as the error type
 *
 * Errors which would end in a Throwable end in a SystemProblem whereas a TimeoutException ends in a TimeoutProblem.
 */
class AlmFuture[+R](val underlying: Future[AlmValidation[R]])(implicit executionContext: akka.dispatch.ExecutionContext) {
  import almhirt.almfuture.all._
  def map[T](compute: R => T): AlmFuture[T] =
    new AlmFuture[T](underlying map { validation => validation map compute })

  def flatMap[T](compute: R => AlmFuture[T]): AlmFuture[T] =
    new AlmFuture(underlying flatMap { validation =>
      validation fold (
        f => Promise.successful(f.failure[T]),
        r => compute(r).underlying)
    })

  def mapV[T](compute: R => AlmValidation[T]): AlmFuture[T] =
    new AlmFuture[T](underlying map { validation => validation bind compute })

  def fold[T](failure: Problem => T = identity[Problem] _, success: R => T = identity[R] _): Future[T] =
    underlying map { validation => validation fold (failure, success) }

  def onComplete(handler: AlmValidation[R] => Unit): AlmFuture[R] = {
    underlying onComplete ({
      case Right(validation) => handler(validation)
      case Left(err) => handler(throwableToProblem(err).failure)
    })
  }

  def onComplete(fail: Problem => Unit, succ: R => Unit): AlmFuture[R] = {
    underlying onComplete ({
      case Right(validation) => validation fold (fail, succ)
      case Left(err) => fail(throwableToProblem(err))
    })
  }

  def sideEffect(fail: Problem => Unit, succ: R => Unit): Unit = {
    underlying onComplete ({
      case Right(validation) => validation fold (fail, succ)
      case Left(err) => fail(throwableToProblem(err))
    })
  }

  def onSuccess(onRes: R => Unit): AlmFuture[R] =
    underlying onSuccess ({
      case x => x fold (_ => (), onRes(_))
    })

  def onFailure(onProb: Problem => Unit): AlmFuture[R] =
    onComplete(_ fold (onProb(_), _ => ()))

  def andThen(effect: AlmValidation[R] => Unit): AlmFuture[R] = {
    new AlmFuture(underlying andThen {
      case Right(r) => effect(r)
      case Left(err) => effect(throwableToProblem(err).failure)
    })
  }

  def isCompleted = underlying.isCompleted

  @deprecated("Use awaitResult", "0.0.1")
  def result(implicit atMost: Duration): AlmValidation[R] = awaitResult

  def awaitResult(implicit atMost: Duration): AlmValidation[R] =
    try {
      Await.result(underlying, atMost)
    } catch {
      case exn => throwableToProblem(exn).failure
    }

  private def throwableToProblem(throwable: Throwable): Problem =
    throwable match {
      case tout: TimeoutException => OperationTimedOutProblem("A future operation timed out: %s".format(tout.getMessage), cause = Some(CauseIsThrowable(tout)))
      case exn => UnspecifiedProblem(exn.getMessage, severity = Major, category = SystemProblem, cause = Some(CauseIsThrowable(exn)))
    }
}

object AlmFuture {
  def apply[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = new AlmFuture[T](Future { compute })
  @deprecated("Use AlmPromise.apply", "0.0.1")
  def promise[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = new AlmFuture[T](Promise.successful { compute })
  @deprecated("Use apply", "0.0.1")
  def future[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = apply(compute)(executor)

  def sequenceAkka[A, M[_] <: Traversable[_]](in: M[AlmFuture[A]])(implicit cbf: CanBuildFrom[M[AlmFuture[A]], AlmValidation[A], M[AlmValidation[A]]], executor: ExecutionContext): Future[M[AlmValidation[A]]] = {
    in.foldLeft(Promise.successful(cbf(in)): Future[Builder[AlmValidation[A], M[AlmValidation[A]]]])((futAcc, futElem) ⇒ for (acc ← futAcc; a ← futElem.asInstanceOf[AlmFuture[A]].underlying) yield (acc += a)).map(_.result)
  }
  
  def sequence[A, M[_] <: Traversable[_]](in: M[AlmFuture[A]])(implicit cbf: CanBuildFrom[M[AlmFuture[A]], AlmValidation[A], M[AlmValidation[A]]], executor: ExecutionContext): AlmFuture[M[AlmValidation[A]]] = {
    val fut = sequenceAkka(in)
    new AlmFuture(fut.map(_.success))
  }
  
}

object AlmPromise {
  def apply[T](result: AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = new AlmFuture[T](Promise.successful { result })
  def successful[T](result: T)(implicit executor: akka.dispatch.ExecutionContext) = apply { result.success }
  def failed[T](prob: Problem)(implicit executor: akka.dispatch.ExecutionContext) = apply { prob.failure[T] }
}