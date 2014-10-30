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
import scalaz.Validation.FlatMap._
import scala.concurrent.{ Future, Promise, Await, ExecutionContext }
import scala.concurrent.duration.Duration
import scalaz.Validation.FlatMap
import almhirt.common._
import almhirt.almfuture.all.akkaFutureToAlmhirtFuture
import almhirt.problem.CauseIsThrowable
import almhirt.problem.HasAThrowable

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

  /** Map the contents to this Future to another content */
  def map[T](compute: R ⇒ T)(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture[T](underlying.map(validation ⇒ validation map compute))

  /** Map the underlying validation. A failure will cause the Future to fail */
  def mapV[T](compute: R ⇒ AlmValidation[T])(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture[T](underlying.map { validation ⇒ validation flatMap compute })

  /** Map the underlying Problem in case of a failure */
  def leftMap(withFailure: Problem ⇒ Problem)(implicit executionContext: ExecutionContext): AlmFuture[R] = {
    val p = Promise[AlmValidation[R]]
    underlying.onComplete {
      case scala.util.Failure(exn) ⇒
        p complete (scala.util.Success(withFailure(handleThrowable(exn)).failure))
      case scala.util.Success(validation) ⇒
        p complete (scala.util.Success(validation fold (p ⇒ withFailure(p).failure, success ⇒ success.success)))
    }
    new AlmFuture(p.future)
  }

  /** Alias for leftMap */
  def mapFailure(withFailure: Problem ⇒ Problem)(implicit executionContext: ExecutionContext): AlmFuture[R] =
    leftMap(withFailure)

  /** Map the underlying Problem in case it is a Timeout */
  def mapTimeout(withTimeout: Problem ⇒ Problem)(implicit executionContext: ExecutionContext): AlmFuture[R] = {
    val p = Promise[AlmValidation[R]]
    underlying.onComplete {
      case scala.util.Failure(exn) ⇒
        handleThrowable(exn) match {
          case OperationTimedOutProblem(prob) ⇒ p complete (scala.util.Success(withTimeout(prob).failure))
          case prob ⇒ p failure (exn)
        }
      case scala.util.Success(validation) ⇒
        validation fold (
          fail ⇒ fail match {
            case OperationTimedOutProblem(prob) ⇒ p complete (scala.util.Success(withTimeout(prob).failure))
            case prob ⇒ p complete scala.util.Success(prob.failure)
          },
          succ ⇒ p complete scala.util.Success(succ.success))
    }
    new AlmFuture(p.future)
  }

  /** Change the message of a timeout */
  def mapTimeoutMessage(newMessage: String ⇒ String)(implicit executionContext: ExecutionContext): AlmFuture[R] =
    new AlmFuture[R](underlying.map { validation ⇒
      validation leftMap {
        case OperationTimedOutProblem(p) ⇒
          p.withMessage(newMessage(p.message))
        case p ⇒
          p
      }
    })

  def flatMap[T](compute: R ⇒ AlmFuture[T])(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture(underlying.flatMap { validation ⇒
      validation fold (
        f ⇒ Future.successful(f.failure[T]),
        r ⇒ compute(r).underlying)
    })

  /** This has no real usage, but is necessary for for-comprehensions */
  def filter(pred: R ⇒ Boolean)(implicit executor: ExecutionContext): AlmFuture[R] =
    mapV {
      r ⇒ if (pred(r)) r.success else NoSuchElementProblem("AlmFuture.filter predicate is not satisfied").failure
    }

  final def withFilter(p: R ⇒ Boolean)(implicit executor: ExecutionContext): AlmFuture[R] = filter(p)(executor)

  /** Make the result or problem something else but stay in the future context */
  def fold[T](failure: Problem ⇒ T, success: R ⇒ T)(implicit executionContext: ExecutionContext): AlmFuture[T] = {
    val p = Promise[AlmValidation[T]]
    underlying.onComplete {
      case scala.util.Failure(exn) ⇒
        p complete (scala.util.Success(failure(handleThrowable(exn)).success))
      case scala.util.Success(validation) ⇒
        p complete (scala.util.Success((validation fold (failure, success)).success))
    }
    new AlmFuture(p.future)
  }

  /** Make the result or problem something else but stay in the future context */
  def foldV[T](failure: Problem ⇒ AlmValidation[T], success: R ⇒ AlmValidation[T])(implicit executionContext: ExecutionContext): AlmFuture[T] = {
    val p = Promise[AlmValidation[T]]
    underlying.onComplete {
      case scala.util.Failure(exn) ⇒
        p complete (scala.util.Success(failure(handleThrowable(exn))))
      case scala.util.Success(validation) ⇒
        p complete (scala.util.Success((validation fold (failure, success))))
    }
    new AlmFuture(p.future)
  }

  /** Fold the content in the future context */
  def foldF[T](failure: Problem ⇒ AlmFuture[T], success: R ⇒ AlmFuture[T])(implicit executionContext: ExecutionContext): AlmFuture[T] = {
    val p = Promise[AlmValidation[T]]
    underlying.onComplete {
      case scala.util.Failure(exn) ⇒
        p completeWith failure(handleThrowable(exn)).underlying
      case scala.util.Success(validation) ⇒
        p completeWith (validation fold (failure, success)).underlying
    }
    new AlmFuture(p.future)
  }

  def collect[T](pf: PartialFunction[R, T])(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture(
      underlying.map(validation ⇒
        validation map (v ⇒ pf(v))))

  def collectV[T](pf: PartialFunction[R, AlmValidation[T]])(implicit executionContext: ExecutionContext): AlmFuture[T] =
    new AlmFuture(
      underlying.map(validation ⇒
        validation flatMap (v ⇒ pf(v))))

  def collectF[T](pf: PartialFunction[R, AlmFuture[T]])(implicit executionContext: ExecutionContext): AlmFuture[T] = {
    val p = Promise[AlmValidation[T]]
    underlying.onComplete {
      case scala.util.Success(validation) ⇒
        validation fold (
          fail ⇒ p complete (scala.util.Success(fail.failure)),
          succ ⇒ p completeWith (pf(succ).underlying))
      case scala.util.Failure(exn) ⇒
        p failure (exn)
    }
    new AlmFuture(p.future)
  }

  /** Act on completion */
  def onComplete(handler: AlmValidation[R] ⇒ Unit)(implicit executionContext: ExecutionContext): Unit = {
    underlying.onComplete {
      case scala.util.Success(validation) ⇒ handler(validation)
      case scala.util.Failure(err) ⇒ handler(handleThrowable(err).failure)
    }
  }

  /** Act on completion */
  def onComplete(fail: Problem ⇒ Unit, succ: R ⇒ Unit)(implicit executionContext: ExecutionContext): Unit = {
    underlying.onComplete {
      case scala.util.Success(validation) ⇒ validation fold (fail, succ)
      case scala.util.Failure(err) ⇒ fail(handleThrowable(err))
    }
  }

  /** Use when only interested in a success and a failure result doesn't matter */
  def onSuccess(onSucc: R ⇒ Unit)(implicit executionContext: ExecutionContext): Unit =
    onComplete(_ fold (_ ⇒ (), onSucc))

  /** As soon as a success is known, schedule the effect */
  def successEffect(effect: R ⇒ Unit)(implicit executionContext: ExecutionContext): AlmFuture[R] =
    andThen { _.fold(_ ⇒ (), succ ⇒ effect(succ)) }

  @deprecated(message = "Use onSuccessWithRecoveredFailure", since = "0.7.1")
  def onSuccessWithRejoinedFailure[U >: R](rejoin: Problem ⇒ U, onRes: U ⇒ Unit)(implicit executionContext: ExecutionContext): Unit =
    this.recover(rejoin).onSuccess(onRes)

  /** Use when only interested in a success and a failure can be converted to a success to rejoin with the happy path */
  def onSuccessWithRecoveredFailure[U >: R](rejoin: Problem ⇒ U, onRes: U ⇒ Unit)(implicit executionContext: ExecutionContext): Unit =
    this.recover(rejoin).onSuccess(onRes)

  /** Use when only interested in a failure and a successful result doesn't matter */
  def onFailure(onProb: Problem ⇒ Unit)(implicit executionContext: ExecutionContext): Unit =
    onComplete(_ fold (onProb, _ ⇒ ()))

  def andThen(effect: AlmValidation[R] ⇒ Unit)(implicit executionContext: ExecutionContext): AlmFuture[R] = {
    new AlmFuture(underlying.andThen {
      case scala.util.Success(r) ⇒ effect(r)
      case scala.util.Failure(err) ⇒ effect(handleThrowable(err).failure)
    })
  }

  def andThen(fail: Problem ⇒ Unit, succ: R ⇒ Unit)(implicit executionContext: ExecutionContext): AlmFuture[R] = {
    new AlmFuture(underlying.andThen {
      case scala.util.Success(r) ⇒ r.fold(fail, succ)
      case scala.util.Failure(err) ⇒ fail(handleThrowable(err))
    })
  }

  @deprecated(message = "Use failureEffect", since = "0.5.210")
  def withFailure(effect: Problem ⇒ Unit)(implicit executionContext: ExecutionContext): AlmFuture[R] =
    failureEffect(effect)

  /** As soon as a failure is known, schedule the effect */
  def failureEffect(effect: Problem ⇒ Unit)(implicit executionContext: ExecutionContext): AlmFuture[R] =
    andThen { _.fold(effect, succ ⇒ ()) }

  @deprecated(message = "Use recover", since = "0.7.1")
  def rejoinFailure[U >: R](rejoin: Problem ⇒ U)(implicit executionContext: ExecutionContext): AlmFuture[U] = {
    this.fold[U](
      rejoin,
      succ ⇒ succ)
  }

  /** In case of a failure, rejoin with the happy path */
  def recover[U >: R](recover: Problem ⇒ U)(implicit executionContext: ExecutionContext): AlmFuture[U] = {
    this.fold[U](
      recover,
      succ ⇒ succ)
  }

  /** In case of a failure, rejoin with the happy path */
  def mapRecover[U](map: R => U, recover: Problem ⇒ U)(implicit executionContext: ExecutionContext): AlmFuture[U] = {
    this.fold[U](
      recover,
      succ ⇒ map(succ))
  }

  /** extract an U from the success. In case of a failure, rejoin with the happy path */
  def collectRecover[U](collect: PartialFunction[R, U], recover: Problem ⇒ U)(implicit executionContext: ExecutionContext): AlmFuture[U] = {
    this.fold[U](
      recover,
      succ ⇒ collect(succ))
  }

  /** A success becaomes a failure */
  def divertToFailure(divert: PartialFunction[R, Problem])(implicit executionContext: ExecutionContext): AlmFuture[R] = {
    this.foldV(
      fail ⇒ fail.failure,
      succ ⇒ if (divert.isDefinedAt(succ)) {
        divert(succ).failure
      } else {
        succ.success
      })
  }

  def extractDivert[U](extract: PartialFunction[R, U], divert: PartialFunction[R, Problem])(implicit executionContext: ExecutionContext): AlmFuture[U] = {
    this.foldV(
      fail ⇒ fail.failure,
      succ ⇒ if (divert.isDefinedAt(succ)) {
        divert(succ).failure
      } else if (extract.isDefinedAt(succ)) {
        extract(succ).success
      } else {
        UnspecifiedProblem(s"""${succ} is neither handled by extract nor by divert.""").failure
      })
  }

  def isCompleted = underlying.isCompleted

  def awaitResult(atMost: Duration): AlmValidation[R] =
    try {
      Await.result(underlying, atMost)
    } catch {
      case exn: Exception ⇒ launderException(exn).failure
    }

  def awaitResultOrEscalate(atMost: Duration): R = {
    import almhirt.syntax.almvalidation._
    awaitResult(atMost).resultOrEscalate
  }

  def withTimeout(maxDuration: scala.concurrent.duration.FiniteDuration, createMessage: scala.concurrent.duration.FiniteDuration ⇒ String = t ⇒ s"A timeout occured after ${t}.")(implicit executionContext: ExecutionContext): AlmFuture[R] = {
    val p = Promise[AlmValidation[R]]

    this.onComplete(
      fail ⇒ p.complete(scala.util.Success(fail.failure)),
      succ ⇒ p.complete(scala.util.Success(succ.success)))

    val timer = new java.util.Timer()
    val r = new java.util.TimerTask() {
      def run() {
        p.complete(scala.util.Success(OperationTimedOutProblem(createMessage(maxDuration)).failure))
      }
    }
    timer.schedule(r, maxDuration.toMillis)

    new AlmFuture(p.future)
  }

  /** Convert this future to a future of the std lib */
  def std(implicit executionContext: ExecutionContext): Future[R] = {
    val p = Promise[R]
    onComplete(
      fail ⇒ {
        val res: Throwable =
          fail match {
            case sp: SingleProblem ⇒
              sp match {
                case ExceptionCaughtProblem(_) ⇒
                  sp.cause match {
                    case Some(CauseIsThrowable(HasAThrowable(exn))) ⇒ exn
                    case _ ⇒ new EscalatedProblemException(sp)
                  }
                case _ ⇒ new EscalatedProblemException(sp)
              }
            case pr ⇒ new EscalatedProblemException(pr)
          }
        p.complete(scala.util.Failure(res))
      },
      succ ⇒ p.complete(scala.util.Success(succ)))
    p.future
  }

  def toStdFuture(implicit executionContext: ExecutionContext): Future[R] = this.std

}

object AlmFuture {
  import scala.language.higherKinds

  /** Start a computation which can fail */
  def apply[T](compute: ⇒ AlmValidation[T])(implicit executionContext: ExecutionContext) = new AlmFuture[T](Future { compute }(executionContext))

  /**
   * Take an M of futures and get a future that completes as a whole sequence, once all futures in M completed
   *
   */
  def sequenceAkka[A, M[_] <: Traversable[_]](in: M[AlmFuture[A]])(implicit cbf: CanBuildFrom[M[AlmFuture[A]], AlmValidation[A], M[AlmValidation[A]]], executionContext: ExecutionContext): Future[M[AlmValidation[A]]] = {
    in.foldLeft(Future.successful(cbf(in)): Future[Builder[AlmValidation[A], M[AlmValidation[A]]]])((futAcc, futElem) ⇒ for (acc ← futAcc; a ← futElem.asInstanceOf[AlmFuture[A]].underlying) yield (acc += a)).map(_.result)
  }

  /**
   * Take a sequence of futures and get a future that completes as a whole sequence, once all futures in the sequence completed
   *
   */
  def sequence[A](in: Seq[AlmFuture[A]])(implicit executionContext: ExecutionContext): AlmFuture[Seq[A]] = {
    import almhirt.almvalidation.kit._
    import scalaz._, Scalaz._
    val underlyings = in.map(x ⇒ x.underlying).toVector
    val fut = Future.sequence(underlyings).map(seq ⇒ seq.map(_.toAgg).sequence)
    new AlmFuture(fut)
  }

  /** Start a computation that is not expected to fail */
  def compute[T](computation: ⇒ T)(implicit executionContext: ExecutionContext) = new AlmFuture[T](Future { inTryCatch(computation) })

  /** Return a future where the result is already known */
  def completed[T](what: ⇒ AlmValidation[T]) = new AlmFuture[T](Future.successful { unsafe(what) })

  /** Return a future where the successful result is already known */
  def successful[T](result: ⇒ T) = new AlmFuture[T](Future.successful { inTryCatch(result) })

  /** Return a future where a failure is already known */
  def failed[T](prob: ⇒ Problem) = new AlmFuture[T](Future.successful {
    try {
      prob.failure
    } catch {
      case scala.util.control.NonFatal(exn) ⇒ launderException(exn).failure
    }
  })

  /** Returns the result after the given duration */
  def delayed[T](duration: scala.concurrent.duration.FiniteDuration)(result: ⇒ AlmValidation[T]): AlmFuture[T] = {
    val p = Promise[AlmValidation[T]]
    val timer = new java.util.Timer()
    val r = new java.util.TimerTask() { def run() { p.complete(scala.util.Success(result)) } }
    timer.schedule(r, duration.toMillis)
    new AlmFuture(p.future)
  }

  /** Returns the value after the given duration */
  def delayedSuccess[T](duration: scala.concurrent.duration.FiniteDuration)(result: ⇒ T): AlmFuture[T] = {
    delayed(duration)(result.success)
  }

  /** Returns the failure with the given Problem after the given duration */
  def delayedFailure[T](duration: scala.concurrent.duration.FiniteDuration)(problem: ⇒ Problem): AlmFuture[Nothing] = {
    delayed(duration)(problem.failure)
  }

}
