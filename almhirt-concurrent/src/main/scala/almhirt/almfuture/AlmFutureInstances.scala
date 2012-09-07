package almhirt.almfuture

import akka.dispatch.Future
import almhirt.AlmValidation
import almhirt.AlmValidation
import almhirt.AlmFuture


trait AlmFutureInstances {
  /** Turn this [[akka.dispatch.Future]] into an [[almhirt.concurrent.Future]] */
  implicit def akkaFutureToAlmhirtFuture[T](akkaFuture: Future[AlmValidation[T]])(implicit executionContext: akka.dispatch.ExecutionContext): AlmFuture[T] =
    new AlmFuture(akkaFuture)
}