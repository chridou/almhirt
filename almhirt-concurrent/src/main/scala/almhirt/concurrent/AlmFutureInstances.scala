package almhirt.concurrent

import akka.dispatch.{Future, Promise}
import almhirt._


trait AlmFutureInstances {
  /** Turn this [[akka.dispatch.Future]] into an [[almhirt.concurrent.Future]] */
  implicit def akkaFutureToAlmhirtFuture[T](akkaFuture: Future[AlmValidation[T]])(implicit executionContext: akka.dispatch.ExecutionContext): AlmFuture[T] =
    new AlmFuture(akkaFuture)
}