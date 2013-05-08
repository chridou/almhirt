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
package almhirt.common

import java.util.concurrent.TimeoutException
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable.Builder
import scalaz.syntax.validation._
import scala.concurrent.{ Future, Promise, Await, ExecutionContext }
import scala.concurrent.duration.Duration
import almhirt.common._
import almhirt.almfuture.all.akkaFutureToAlmhirtFuture

/**
 * A future based on [[akka.dispatch.Future]].
 *
 * The intention is to have a future that doesn't rely on the Either type where Left[Throwable] identifies an error.
 * Instead a result should always be in a [[almhirt.validation.AlmValidation]] which is in fact a [[scalaz.Validation]]
 * based on [[almhirt.validation.Problem]] as the error type
 *
 * Errors which would end in a Throwable end in a SystemProblem whereas a TimeoutException ends in a TimeoutProblem.
 */
final class AlmFuture[+R](val underlying: Future[AlmValidation[R]]) {
  import almhirt.almfuture.all._
  def map[T](compute: R => T)(implicit hasExecutionContext: HasExecutionContext): AlmFuture[T] =
    new AlmFuture[T](underlying.map(validation => validation map compute)(hasExecutionContext.executionContext))

  def mapV[T](compute: R => AlmValidation[T])(implicit hasExecutionContext: HasExecutionContext): AlmFuture[T] =
    new AlmFuture[T](underlying.map { validation => validation flatMap compute }(hasExecutionContext.executionContext))

  def flatMap[T](compute: R => AlmFuture[T])(implicit hasExecutionContext: HasExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.flatMap { validation =>
      validation fold (
        f => Future.successful(f.failure[T]),
        r => compute(r).underlying)
    }(hasExecutionContext.executionContext))

  def fold[T](failure: Problem => T, success: R => T)(implicit hasExecutionContext: HasExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.map { validation => (validation fold (failure, success)).success }(hasExecutionContext.executionContext))

  def foldV[T](failure: Problem => AlmValidation[T], success: R => AlmValidation[T])(implicit hasExecutionContext: HasExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.map { validation => (validation fold (failure, success)) }(hasExecutionContext.executionContext))

  def foldF[T](failure: Problem => AlmFuture[T], success: R => AlmFuture[T])(implicit hasExecutionContext: HasExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.flatMap { validation =>
      validation fold (
        f => failure(f).underlying,
        r => success(r).underlying)
    }(hasExecutionContext.executionContext))
  
  def onComplete(handler: AlmValidation[R] => Unit)(implicit hasExecutionContext: HasExecutionContext): Unit = {
    underlying.onComplete {
      case scala.util.Success(validation) => handler(validation)
      case scala.util.Failure(err) => handler(handleThrowable(err).failure)
    }(hasExecutionContext.executionContext)
  }

  def onComplete(fail: Problem => Unit, succ: R => Unit)(implicit hasExecutionContext: HasExecutionContext): Unit = {
    underlying.onComplete {
      case scala.util.Success(validation) => validation fold (fail, succ)
      case scala.util.Failure(err) => fail(handleThrowable(err))
    }(hasExecutionContext.executionContext)
  }

  def onSuccess(onRes: R => Unit)(implicit hasExecutionContext: HasExecutionContext): Unit =
    underlying.onSuccess {
      case x => x fold (_ => (), onRes(_))
    }(hasExecutionContext.executionContext)

  def onFailure(onProb: Problem => Unit)(implicit hasExecutionContext: HasExecutionContext): Unit =
    onComplete(_ fold (onProb(_), _ => ()))

  def andThen(effect: AlmValidation[R] => Unit)(implicit hasExecutionContext: HasExecutionContext): AlmFuture[R] = {
    new AlmFuture(underlying.andThen {
      case scala.util.Success(r) => effect(r)
      case scala.util.Failure(err) => effect(handleThrowable(err).failure)
    }(hasExecutionContext.executionContext))
  }

  def andThen(fail: Problem => Unit, succ: R => Unit)(implicit hasExecutionContext: HasExecutionContext): AlmFuture[R] = {
    new AlmFuture(underlying.andThen {
      case scala.util.Success(r) => r.fold(fail, succ)
      case scala.util.Failure(err) => fail(handleThrowable(err))
    }(hasExecutionContext.executionContext))
  }
  
  def withFailure(effect: Problem => Unit)(implicit hasExecutionContext: HasExecutionContext): AlmFuture[R] = 
    andThen{ _.fold(prob => effect(prob), succ => ()) }

  def isCompleted = underlying.isCompleted

  def awaitResult(implicit atMost: Duration): AlmValidation[R] =
    try {
      Await.result(underlying, atMost)
    } catch {
      case exn: Exception => launderException(exn).failure
    }
}

object AlmFuture {
  import scala.language.higherKinds
  
  def apply[T](compute: => AlmValidation[T])(implicit hasExecutionContext: HasExecutionContext) = new AlmFuture[T](Future { compute }(hasExecutionContext.executionContext))

  def sequenceAkka[A, M[_] <: Traversable[_]](in: M[AlmFuture[A]])(implicit cbf: CanBuildFrom[M[AlmFuture[A]], AlmValidation[A], M[AlmValidation[A]]], hasExecutionContext: HasExecutionContext): Future[M[AlmValidation[A]]] = {
    implicit val executionContext = hasExecutionContext.executionContext
    in.foldLeft(Future.successful(cbf(in)): Future[Builder[AlmValidation[A], M[AlmValidation[A]]]])((futAcc, futElem) ⇒ for (acc ← futAcc; a ← futElem.asInstanceOf[AlmFuture[A]].underlying) yield (acc += a)).map(_.result)
  }
  
  def sequence[A, M[_] <: Traversable[_]](in: M[AlmFuture[A]])(implicit cbf: CanBuildFrom[M[AlmFuture[A]], AlmValidation[A], M[AlmValidation[A]]], hasExecutionContext: HasExecutionContext): AlmFuture[M[AlmValidation[A]]] = {
    val fut = sequenceAkka(in)
    new AlmFuture(fut.map(_.success)(hasExecutionContext.executionContext))
  }

  def promise[T](what: => AlmValidation[T]) = new AlmFuture[T](Future.successful { what })
  def successful[T](result: => T) = new AlmFuture[T](Future.successful { result.success })
  def failed[T](prob: Problem) = new AlmFuture[T](Future.successful { prob.failure })
  
}
