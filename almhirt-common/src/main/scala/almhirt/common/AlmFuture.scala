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
 * Errors which would end in a Throwable end in a Problem .
 */
final class AlmFuture[+R](val underlying: Future[AlmValidation[R]]) {
  import almhirt.almfuture.all._
  def map[T](compute: R => T)(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture[T](underlying.map(validation => validation map compute)(executionContext))

  def mapV[T](compute: R => AlmValidation[T])(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture[T](underlying.map { validation => validation flatMap compute }(executionContext))

  def flatMap[T](compute: R => AlmFuture[T])(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.flatMap { validation =>
      validation fold (
        f => Future.successful(f.failure[T]),
        r => compute(r).underlying)
    }(executionContext))

  def fold[T](failure: Problem => T, success: R => T)(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.map { validation => (validation fold (failure, success)).success }(executionContext))

  def foldV[T](failure: Problem => AlmValidation[T], success: R => AlmValidation[T])(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.map { validation => (validation fold (failure, success)) }(executionContext))

  def foldF[T](failure: Problem => AlmFuture[T], success: R => AlmFuture[T])(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.flatMap { validation =>
      validation fold (
        f => failure(f).underlying,
        r => success(r).underlying)
    }(executionContext))
  
  def onComplete(handler: AlmValidation[R] => Unit)(implicit executionContext: ExecutionContext): Unit = {
    underlying.onComplete {
      case scala.util.Success(validation) => handler(validation)
      case scala.util.Failure(err) => handler(handleThrowable(err).failure)
    }(executionContext)
  }

  def onComplete(fail: Problem => Unit, succ: R => Unit)(implicit executionContext: ExecutionContext): Unit = {
    underlying.onComplete {
      case scala.util.Success(validation) => validation fold (fail, succ)
      case scala.util.Failure(err) => fail(handleThrowable(err))
    }(executionContext)
  }

  def onSuccess(onRes: R => Unit)(implicit executionContext: ExecutionContext): Unit =
    underlying.onSuccess {
      case x => x fold (_ => (), onRes(_))
    }(executionContext)

  def onFailure(onProb: Problem => Unit)(implicit executionContext: ExecutionContext): Unit =
    onComplete(_ fold (onProb(_), _ => ()))

  def andThen(effect: AlmValidation[R] => Unit)(implicit executionContext: ExecutionContext): AlmFuture[R] = {
    new AlmFuture(underlying.andThen {
      case scala.util.Success(r) => effect(r)
      case scala.util.Failure(err) => effect(handleThrowable(err).failure)
    }(executionContext))
  }

  def andThen(fail: Problem => Unit, succ: R => Unit)(implicit executionContext: ExecutionContext): AlmFuture[R] = {
    new AlmFuture(underlying.andThen {
      case scala.util.Success(r) => r.fold(fail, succ)
      case scala.util.Failure(err) => fail(handleThrowable(err))
    }(executionContext))
  }
  
  def withFailure(effect: Problem => Unit)(implicit executionContext: ExecutionContext): AlmFuture[R] = 
    andThen{ _.fold(prob => effect(prob), succ => ()) }

  def isCompleted = underlying.isCompleted

  def awaitResult(atMost: Duration): AlmValidation[R] =
    try {
      Await.result(underlying, atMost)
    } catch {
      case exn: Exception => launderException(exn).failure
    }
  def awaitResultOrEscalate(atMost: Duration): R = {
    import almhirt.syntax.almvalidation._
    awaitResult(atMost).resultOrEscalate
  }
}

object AlmFuture {
  import scala.language.higherKinds
  
  def apply[T](compute: => AlmValidation[T])(implicit executionContext: ExecutionContext) = new AlmFuture[T](Future { compute }(executionContext))

  def sequenceAkka[A, M[_] <: Traversable[_]](in: M[AlmFuture[A]])(implicit cbf: CanBuildFrom[M[AlmFuture[A]], AlmValidation[A], M[AlmValidation[A]]], executionContext: ExecutionContext): Future[M[AlmValidation[A]]] = {
    in.foldLeft(Future.successful(cbf(in)): Future[Builder[AlmValidation[A], M[AlmValidation[A]]]])((futAcc, futElem) ⇒ for (acc ← futAcc; a ← futElem.asInstanceOf[AlmFuture[A]].underlying) yield (acc += a)).map(_.result)
  }
  
  def sequence[A](in: Seq[AlmFuture[A]])(implicit executionContext: ExecutionContext): AlmFuture[Seq[A]] = {
    import almhirt.almvalidation.kit._
    import scalaz._, Scalaz._
    val underlyings = in.map(x => x.underlying).toVector
    val fut = Future.sequence(underlyings).map(seq => seq.map(_.toAgg).sequence)
    new AlmFuture(fut)
  }

  def promise[T](what: => AlmValidation[T]) = new AlmFuture[T](Future.successful { what })
  def successful[T](result: => T) = new AlmFuture[T](Future.successful { result.success })
  def failed[T](prob: Problem) = new AlmFuture[T](Future.successful { prob.failure })
  
}
