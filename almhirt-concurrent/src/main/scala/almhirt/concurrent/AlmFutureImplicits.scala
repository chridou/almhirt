package almhirt.concurrent

import scalaz._, Scalaz._
import akka.dispatch.{Future, Promise}
import almhirt.validation._
import almhirt.validation.AlmValidation

trait AlmFutureImplicits {
  implicit def akkaFutureToAlmhirtFuture[T](akkaFuture: Future[AlmValidation[T]])(implicit executionContext: akka.dispatch.ExecutionContext): AlmFuture[T] =
    new AlmFuture(akkaFuture)
  implicit def akkaFutureToAkkaFutureW[T](akkaFuture: Future[Any]) =
    new AkkaFutureAnyW(akkaFuture)
  import scala.reflect._
  class AkkaFutureAnyW(akkaFuture: Future[Any]) {
    def toAlmFuture[T](implicit m: Manifest[T], executionContext: akka.dispatch.ExecutionContext): AlmFuture[T] = 
      new AlmFuture[T](akkaFuture.mapTo[AlmValidation[T]])
  }


  implicit def AlmValidationToalmhirtValidatenW[T](validation: AlmValidation[T]) =
    new AlmhirtValidatenW(validation)
  class AlmhirtValidatenW[T](validation: AlmValidation[T]) {
    def continueAsync[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      validation fold(
        prob => new AlmFuture(Promise.successful(prob.failure)), 
        r => new AlmFuture(Future[AlmValidation[U]]{compute(r)}))

    def |~> [U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      continueAsync[U](compute)(executor)

    def doAsync(failure: Problem => Unit, sideEffect: T => Unit)(implicit executor: akka.dispatch.ExecutionContext): Unit =
      validation fold(
        prob => failure(prob), 
        r => executor.execute(new Runnable{def run() = sideEffect(r)}))
      
    def continueWithPromise[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      validation fold(
        prob => new AlmFuture(Promise.successful(prob.failure)), 
        r => new AlmFuture(Promise.successful(compute(r))))
        
    def |->[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      continueWithPromise[U](compute)(executor)
    
    def continueWithFuture[U](futureComputation: T => AlmFuture[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      validation fold(
        prob => new AlmFuture(Promise.successful((prob.failure))), 
        r => futureComputation(r))
    
    def |#>[U](future: T => AlmFuture[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      continueWithFuture[U](future)(executor)
  }
}