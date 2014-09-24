package almhirt.akkax

import almhirt.common._
import akka.actor._
import scala.concurrent.ExecutionContext
import almhirt.problem.CauseIsThrowable

trait AlmActorSupport { me: Actor =>
  implicit class AlmFuturePipeTo[T](self: AlmFuture[T]) {
    def pipeTo(receiver: ActorRef, unwrapProblem: Boolean = true)(implicit executionContext: ExecutionContext) {
      import almhirt.problem._
      self.onComplete(
        problem =>
          problem match {
            case IsSingleProblem(ContainsThrowable(throwable)) if unwrapProblem =>
              receiver ! Status.Failure(throwable)
            case _ =>
              receiver ! Status.Failure(new EscalatedProblemException(problem))
          },
        succ => receiver ! succ)
    }
  }
}