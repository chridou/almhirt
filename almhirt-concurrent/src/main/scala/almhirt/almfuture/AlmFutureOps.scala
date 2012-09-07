package almhirt.almfuture

import akka.dispatch.Future
import akka.dispatch.Promise
import scala.reflect.Manifest
import scalaz.Scalaz.ToValidationV
import scalaz.syntax.Ops
import almhirt._
import almvalidation.kit._

/** Implicits on an untyped [[akka.dispatch.Future]] */
trait AlmFutureOps0 extends Ops[Future[Any]] {
  import scala.reflect._
  /** Turn this a [[akka.dispatch.Future]] into on AlmFuture of the given type 
   * 
   * '''The real type of the [[akka.dispatch.Future]] must be [[almhirt.validation.AlmValidation[T]]]'''
   * 
   * @tparam T The success type in [[almhirt.validation.AlmValidation[T]]]
   */
  def toAlmFuture[T](implicit m: Manifest[T], executionContext: akka.dispatch.ExecutionContext): AlmFuture[T] = 
    new AlmFuture[T](self.mapTo[AlmValidation[T]])
}


/** Operations for starting a future operation from a [[almhirt.validation.AlmValidation]] */
trait AlmFutureOps1[T] extends Ops[AlmValidation[T]] {
  /** In case of success start the given computation otherwise return the Failure 
   * 
   * @param compute The function to execute async
   * @return The future containing the eventual result
   */
  def continueAsync[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
    self fold(
      prob => new AlmFuture(Promise.successful(prob.failure)), 
      r => new AlmFuture(Future[AlmValidation[U]]{compute(r)}))

  /** In case of success start the given computation otherwise return the Failure 
   * 
   * @param compute The function to execute async
   * @return The future containing the eventual result
   */
  def |~> [U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
    continueAsync[U](compute)(executor)

  /** In case of success start the given side effect otherwise return the Failure 
   * 
   * @param compute The side effect to execute async
   */
  def doAsync(failure: Problem => Unit, sideEffect: T => Unit)(implicit executor: akka.dispatch.ExecutionContext): Unit =
    self fold(
      prob => failure(prob), 
      r => executor.execute(new Runnable{def run() = sideEffect(r)}))
  /** In case of success start the given side effect otherwise return the Failure 
   * 
   * @param compute The side effect to execute async
   */
  def ~| (failure: Problem => Unit, sideEffect: T => Unit)(implicit executor: akka.dispatch.ExecutionContext): Unit =
    doAsync(failure, sideEffect)(executor)

  /** In case of a success: Execute the computation as an already computed result
   * 
   * @param compute The computation
   */
  def continueWithPromise[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
    self fold(
      prob => new AlmFuture(Promise.successful(prob.failure)), 
      r => new AlmFuture(Promise.successful(compute(r))))
        
  /** Execute the computation as an already computed result
   * 
   * @param compute The computation
   */
  def |->[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
    continueWithPromise[U](compute)(executor)
    
  /** In case of a success: Start the given future
   * 
   * @param compute The computation which eventually returns a result
   */
  def continueWithFuture[U](futureComputation: T => AlmFuture[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
    self fold(
      prob => new AlmFuture(Promise.successful((prob.failure))), 
      r => futureComputation(r))
    
  /** In case of a success: Start the given future
   * 
   * @param compute The computation which eventually returns a result
   */
  def |#>[U](future: T => AlmFuture[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
    continueWithFuture[U](future)(executor)
}

trait ToAlmFutureOps {
  implicit def FromAkkaFutureToAlmFutureOps0(a: Future[Any]): AlmFutureOps0 = new AlmFutureOps0 {def self = a }
  implicit def FromAlmValidationToAlmFutureOps1[T](a: AlmValidation[T]): AlmFutureOps1[T] = new AlmFutureOps1[T]{def self = a }
}