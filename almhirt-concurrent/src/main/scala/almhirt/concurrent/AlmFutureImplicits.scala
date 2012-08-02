package almhirt.concurrent

import scalaz.{Success, Failure}
import akka.dispatch.{Future, Promise}
import almhirt.validation._
import almhirt.validation.AlmValidation
import almhirt.almakka.AlmAkka

trait AlmFutureImplicits {
  implicit def akkaFutureToAlmhirtFuture[T](akkaFuture: Future[AlmValidation[T]]): AlmFuture[T] =
    new AlmFuture(akkaFuture)
//  implicit def almhirtFutureToAkkaFuture[T](akkaFuture: AlmFuture[T]): Future[AlmValidation[T]] =
//    akkaFuture.underlying
  implicit def akkaFutureToAkkaFutureW[T](akkaFuture: Future[Any]) =
    new AkkaFutureAnyW(akkaFuture)
  import scala.reflect._
  class AkkaFutureAnyW(akkaFuture: Future[Any]) {
    def toAlmFuture[T](implicit m: Manifest[T]): AlmFuture[T] = 
      new AlmFuture[T](akkaFuture.mapTo[AlmValidation[T]])
  }


  implicit def AlmValidationToalmhirtValidatenW[T](validation: AlmValidation[T]) =
    new AlmhirtValidatenW(validation)
  class AlmhirtValidatenW[T](validation: AlmValidation[T]) {
    def continueAsync[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      validation match {
        case Success(r) => new AlmFuture(Future[AlmValidation[U]]{compute(r)})
        case Failure(problem) => new AlmFuture(Promise.successful(Failure(problem)))
      }

    def |~> [U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      continueAsync[U](compute)(executor)

    def continueWithPromise[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      validation match {
        case Success(r) => new AlmFuture(Promise.successful(compute(r)))
        case Failure(problem) => new AlmFuture(Promise.successful(Failure(problem)))
      }
    
    def |->[U](compute: T => AlmValidation[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      continueWithPromise[U](compute)(executor)
    
    def continueWithFuture[U](futureComputation: T => AlmFuture[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      validation match {
        case Success(r) => futureComputation(r)
        case Failure(problem) => new AlmFuture(Promise.successful(Failure(problem)))
      }
    
    def |#>[U](future: T => AlmFuture[U])(implicit executor: akka.dispatch.ExecutionContext): AlmFuture[U] =
      continueWithFuture[U](future)(executor)
  }
}