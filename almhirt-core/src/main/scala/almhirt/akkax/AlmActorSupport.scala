package almhirt.akkax

import scala.language.implicitConversions

import scala.concurrent._
import scala.concurrent.duration._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.almfuture.all._
import akka.actor._
import scala.concurrent.ExecutionContext
import almhirt.problem.CauseIsThrowable

trait AlmActorSupport { me: Actor ⇒

  @deprecated("Do not use. Will be removed", "0.7.4")
  def pipeTo[T](what: AlmFuture[T])(receiver: ActorRef, unwrapProblem: Boolean = true)(implicit executionContext: ExecutionContext): AlmFuture[T] = {
    what.pipeTo(receiver, unwrapProblem)
  }

  @deprecated("Do not use. Will be removed", "0.7.4")
  def recoverPipeTo[T](what: AlmFuture[T], recover: Problem ⇒ Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext): AlmFuture[T] = {
    what.recoverPipeTo(recover)(receiver)
  }

  @deprecated("Do not use. Will be removed", "0.7.4")
  def mapRecoverPipeTo[T](what: AlmFuture[T], map: T ⇒ Any, recover: Problem ⇒ Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext): AlmFuture[T] = {
    what.mapRecoverPipeTo(map, recover)(receiver)
  }

  private def innerRetry[T](f: ⇒ AlmFuture[T], lastFailure: Problem, promise: Promise[AlmValidation[T]], retriesLeft: Int, retryDelay: FiniteDuration, executor: ExecutionContext) {
    if (retriesLeft == 0) {
      promise.success(lastFailure.failure)
    } else {
      if (retryDelay == Duration.Zero) {
        f.onComplete(
          fail ⇒ innerRetry(f, fail, promise, retriesLeft - 1, retryDelay, executor),
          succ ⇒ promise.success(succ.success))(executor)
      } else {
        me.context.system.scheduler.scheduleOnce(retryDelay) {
          f.onComplete(
            fail ⇒ innerRetry(f, fail, promise, retriesLeft - 1, retryDelay, executor),
            succ ⇒ promise.success(succ.success))(executor)
        }(executor)
      }
    }
  }

  def retry[T](f: ⇒ AlmFuture[T])(numRetries: Int, retryDelay: FiniteDuration, executor: ExecutionContext = me.context.dispatcher): AlmFuture[T] = {
    if (numRetries >= 0) {
      val p = Promise[AlmValidation[T]]

      f.onComplete(
        fail ⇒ innerRetry(f, fail, p, numRetries, retryDelay, executor),
        succ ⇒ p.success(succ.success))(executor)

      new AlmFuture(p.future)
    } else {
      AlmFuture.failed(ArgumentProblem("numRetries must not be lower than zero!"))
    }
  }

  implicit def almFuture2PipeableFuture[T](future: AlmFuture[T]): PipeableAlmFuture[T] = new PipeableAlmFuture(future)

  class PipeableAlmFuture[T](future: AlmFuture[T]) extends AnyRef {
    def pipeTo(receiver: ActorRef, unwrapProblem: Boolean = true)(implicit executionContext: ExecutionContext): AlmFuture[T] = {
      import almhirt.problem._
      future.onComplete(
        problem ⇒
          problem match {
            case ExceptionCaughtProblem(ContainsThrowable(throwable)) ⇒
              receiver ! Status.Failure(throwable)
            case ContainsThrowable(throwable) if unwrapProblem ⇒
              receiver ! Status.Failure(throwable)
            case _ ⇒
              receiver ! Status.Failure(new EscalatedProblemException(problem))
          },
        succ ⇒ receiver ! succ)
      future
    }

    @deprecated("Use recoverThenPipeTo", since = "0.7.6")
    def recoverPipeTo(recover: Problem ⇒ Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext): AlmFuture[T] =
      recoverThenPipeTo(recover)(receiver)

    def recoverThenPipeTo(recover: Problem ⇒ Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext): AlmFuture[T] = {
      import almhirt.problem._
      future.onComplete(
        problem ⇒ receiver ! recover(problem),
        succ ⇒ receiver ! succ)
      future
    }

    @deprecated("Use mapOrRecoverThenPipeTo", since = "0.7.6")
    def mapRecoverPipeTo(map: T ⇒ Any, recover: Problem ⇒ Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext): AlmFuture[T] =
      mapOrRecoverThenPipeTo(map, recover)(receiver)

    def mapOrRecoverThenPipeTo(map: T ⇒ Any, recover: Problem ⇒ Any)(receiver: ActorRef)(implicit executionContext: ExecutionContext): AlmFuture[T] = {
      import almhirt.problem._
      future.onComplete(
        problem ⇒ receiver ! recover(problem),
        succ ⇒ receiver ! map(succ))
      future
    }
  }
}