package almhirt.akkax

import almhirt.common._
import akka.actor._
import scala.concurrent.ExecutionContext
import almhirt.problem.CauseIsThrowable

trait AlmActorSupport { me: Actor =>
  def pipeTo[T](what: => AlmFuture[T])(receiver: ActorRef, unwrapProblem: Boolean = true)(implicit executionContext: ExecutionContext) {
    what.pipeTo(receiver, unwrapProblem)
  }

  def recoverPipeTo[T](what: => AlmFuture[T], recover: Problem => Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext) {
    what.pipeTo(receiver)
  }

  def mapRecoverPipeTo[T](what: => AlmFuture[T], map: T => Any, recover: Problem => Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext) {
    what.mapRecoverPipeTo(map, recover)(receiver)
  }

  implicit class AlmFuturePipeTo[T](self: AlmFuture[T]) {
    def pipeTo(receiver: ActorRef, unwrapProblem: Boolean = true)(implicit executionContext: ExecutionContext) {
      import almhirt.problem._
      self.onComplete(
        problem =>
          problem match {
            case ExceptionCaughtProblem(ContainsThrowable(throwable)) =>
              receiver ! Status.Failure(throwable)
            case ContainsThrowable(throwable) if unwrapProblem =>
              receiver ! Status.Failure(throwable)
            case _ =>
              receiver ! Status.Failure(new EscalatedProblemException(problem))
          },
        succ => receiver ! succ)
    }

    def recoverPipeTo(recover: Problem => Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext) {
      import almhirt.problem._
      self.onComplete(
        problem => receiver ! recover(problem),
        succ => receiver ! succ)
    }

    def mapRecoverPipeTo(map: T => Any, recover: Problem => Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext) {
      import almhirt.problem._
      self.onComplete(
        problem => receiver ! recover(problem),
        succ => receiver ! map(succ))
    }
  }
}