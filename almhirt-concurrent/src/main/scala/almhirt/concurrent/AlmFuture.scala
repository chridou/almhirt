package almhirt.concurrent

import java.util.concurrent.TimeoutException
import scala.{Left, Right}
import scalaz.{Validation, Success, Failure}
import scalaz.syntax.validation._
import akka.dispatch.{Future, Promise, Await}
import almhirt.validation.{Problem, AlmValidation}
import almhirt.validation.Problem._
import akka.util.Duration

class AlmFuture[+R](val underlying: Future[AlmValidation[R]])(implicit executionContext: akka.dispatch.ExecutionContext)  {
  def map[T](compute: R => T): AlmFuture[T] =
    new AlmFuture[T](underlying map { validation => validation map compute })
    
  def flatMap[T](compute: R => AlmFuture[T]): AlmFuture[T] =
    new AlmFuture(underlying flatMap { validation => 
      validation fold (
        f => Promise.successful(f.failure[T]),
        r => compute(r).underlying) } )

  def mapV[T](compute: R => AlmValidation[T]): AlmFuture[T] =
    new AlmFuture[T](underlying map { validation => validation bind compute })
  
  def fold[T](failure: Problem => T = identity[Problem] _, success: R => T = identity[R] _): Future[T] =
    underlying map { validation => validation fold (failure, success)}
  
  def onComplete(handler: AlmValidation[R] => Unit): AlmFuture[R] = {
    underlying onComplete({
      case Right(validation) => handler(validation)
      case Left(err) => {
        val prob = err match {
          case tout: TimeoutException => OperationTimedOutProblem("A future operation timed out.", exception = Some(tout)) 
          case exn => UnspecifiedSystemProblem(exn.getMessage, exception = Some(exn)) 
        }
        handler(prob.failure[R])
      }
    })
  }

  def onComplete(fail: Problem => Unit, succ: R => Unit): AlmFuture[R] = {
    underlying onComplete({
      case Right(validation) => validation fold(fail, succ)
      case Left(err) => {
        val prob = err match {
          case tout: TimeoutException => OperationTimedOutProblem("A future operation timed out.", exception = Some(tout)) 
          case exn => UnspecifiedSystemProblem(exn.getMessage, exception = Some(exn)) 
        }
        fail(prob)
      }
    })
  }
  
  def onSuccess(onRes: R => Unit): AlmFuture[R] = 
    underlying onSuccess({
      case x => x fold(_ => (), onRes(_))
    })

  def onFailure(onProb: Problem => Unit): AlmFuture[R] = 
    onComplete(_ fold(onProb(_), _ => ()))
 
  def andThen(effect: AlmValidation[R] => Unit): AlmFuture[R] = {
    new AlmFuture(underlying andThen{
      case Right(r) => effect(r)
      case Left(err) => effect(UnspecifiedSystemProblem(err.getMessage, exception = Some(err)).failure[R])
    })
  }
  
  def isCompleted = underlying.isCompleted
  
  def result(implicit atMost: Duration): AlmValidation[R] = 
    try {
      Await.result(underlying, atMost)
    } catch {
      case tout: TimeoutException => OperationTimedOutProblem("A future operation timed out.", exception = Some(tout)).failure[R] 
      case exn => UnspecifiedSystemProblem(exn.getMessage, exception = Some(exn)).failure[R] 
    }
}

object AlmFuture extends AlmFutureImplicits {
  def apply[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = new AlmFuture[T](Promise.successful{compute})
  @deprecated("Use AlmPromise.apply", "0.0.1")
  def promise[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = new AlmFuture[T](Promise.successful{compute})
  @deprecated("Use apply", "0.0.1")
  def future[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = apply(compute)(executor)
}

object AlmPromise{
  def apply[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = new AlmFuture[T](Promise.successful{compute})
  
}