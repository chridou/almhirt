package almhirt.concurrent

import scalaz.{Success, Failure}
import akka.dispatch.{Future, Promise}
import almhirt.validation._
import almhirt.validation.AlmValidation
import almhirt.almakka.AlmAkka

trait AlmFutureImplicits extends AlmAkka {
  implicit def akkaFutureToalmhirtFuture[T](akkaFuture: Future[AlmValidation[T]]): AlmFuture[T] =
    new AlmFuture(akkaFuture)
  implicit def almhirtFutureToAkkaFuture[T](hdrFuture: AlmFuture[T]): Future[AlmValidation[T]] =
    hdrFuture.akkaFuture
  implicit def akkaFutureToAkkaFutureW[T](akkaFuture: Future[Any]) =
    new AkkaFutureAnyW(akkaFuture)
  implicit def AlmValidationToalmhirtValidatenW[T](validation: AlmValidation[T]) =
    new almhirtValidatenW(validation)
    
  import scala.reflect._
  class AkkaFutureAnyW(akkaFuture: Future[Any]) {
    def mapToAlm[T](implicit m: Manifest[T]): AlmFuture[T] = 
      new AlmFuture[T](akkaFuture.mapTo[AlmValidation[T]])
  }
  
  class almhirtValidatenW[T](validation: AlmValidation[T]) {
    def beginAsyncWorkflow[U](compute: T => AlmValidation[U]): AlmFuture[U] =
      validation match {
        case Success(r) => new AlmFuture(Future[AlmValidation[U]]{compute(r)})
        case Failure(problem) => new AlmFuture(Promise.successful(Failure(problem)))
      }
    def beginWorkflowPromising[U](compute: T => AlmValidation[U]): AlmFuture[U] =
      validation match {
        case Success(r) => new AlmFuture(Promise.successful(compute(r)))
        case Failure(problem) => new AlmFuture(Promise.successful(Failure(problem)))
      }
    def continueWithFuture[U](futureComputation: T => AlmFuture[U]): AlmFuture[U] =
      validation match {
        case Success(r) => futureComputation(r)
        case Failure(problem) => new AlmFuture(Promise.successful(Failure(problem)))
      }
  }
}