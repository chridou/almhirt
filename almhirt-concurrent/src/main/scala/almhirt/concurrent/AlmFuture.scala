package almhirt.concurrent

import java.util.concurrent.TimeoutException
import scala.{Left, Right}
import scalaz.{Validation, Success, Failure}
import scalaz.syntax.validation._
import akka.dispatch.{Future, Promise, Await}
import almhirt.almakka.AlmAkka
import almhirt.validation.{Problem, AlmValidation}
import almhirt.validation.Problem._
import akka.util.Duration

class AlmFuture[+R](val underlying: Future[AlmValidation[R]]) extends AlmAkka {
  def map[T](compute: R => T): AlmFuture[T] =
    new AlmFuture[T](underlying map { validation => validation map compute })
    
  def flatMap[T](compute: R => AlmFuture[T]): AlmFuture[T] =
    new AlmFuture(underlying flatMap { validation => 
      validation fold (
        failure = f => Promise.successful(f.fail[T]),
        success = r => compute(r).underlying) } )
  
  def filter(pred: R => Boolean): AlmFuture[R] =
    this
  
  def onComplete(handler: AlmValidation[R] => Unit): Future[AlmValidation[R]] = {
    underlying onComplete({
      case Right(validation) => handler(validation)
      case Left(err) => {
        val prob = err match {
          case tout: TimeoutException => OperationTimedOutProblem("An operation timed out.", exception = Some(tout)) 
          case exn => UnspecifiedSystemProblem(exn.getMessage, exception = Some(exn)) 
        }
        handler(prob.fail[R])
      }
    })
  }
  
  def onSuccess(onRes: R => Unit): AlmFuture[R] = {
    underlying onSuccess({
      case Success(r) => onRes(r)
      case _ => ()})
  }

  def onFailure(onProb: Problem => Unit): AlmFuture[R] = {
    onComplete({
      case Failure(prob) => onProb(prob)
      case _ => ()
    })
  }
 
  def andThen(effect: AlmValidation[R] => Unit): AlmFuture[R] = {
    new AlmFuture(underlying andThen{
      case Right(r) => effect(r)
      case Left(err) => effect(UnspecifiedSystemProblem(err.getMessage, exception = Some(err)).fail[R])
    })
  }
  
  def isCompleted = underlying.isCompleted
  
  def result(implicit atMost: Duration): AlmValidation[R] = 
    try {
      Await.result(underlying, atMost)
    } catch {
      case tout: TimeoutException => OperationTimedOutProblem("An operation timed out.", exception = Some(tout)).fail[R] 
      case exn => UnspecifiedSystemProblem(exn.getMessage, exception = Some(exn)).fail[R] 
    }
}

object AlmFuture extends AlmFutureImplicits {
  def promise[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = new AlmFuture[T](Promise.successful{compute})
  def future[T](compute: => AlmValidation[T])(implicit executor: akka.dispatch.ExecutionContext) = new AlmFuture[T](Future{compute})
}