package almhirt.akkax

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.configuration.{ RetrySettings, TimeLimitedRetrySettings, AttemptLimitedRetrySettings }

object RetryActor {
  def props[T](
    settings: RetrySettings,
    toTry: () ⇒ AlmFuture[T],
    onSuccess: T ⇒ Unit,
    onFailedAttempt: (FiniteDuration, Int, Problem) ⇒ Unit,
    onFailedLoop: (FiniteDuration, Int, Problem) ⇒ Unit,
    onFinalFailure: (FiniteDuration, Int, Problem) ⇒ Unit): Props =
    Props(new RetryActorImpl(settings, toTry, onSuccess, onFailedAttempt, onFailedLoop, onFinalFailure))
}

private[almhirt] class RetryActorImpl[T](
  settings: RetrySettings,
  toTry: () ⇒ AlmFuture[T],
  onSuccess: T ⇒ Unit,
  onFailedAttempt: (FiniteDuration, Int, Problem) ⇒ Unit,
  onFailedLoop: (FiniteDuration, Int, Problem) ⇒ Unit,
  onFinalFailure: (FiniteDuration, Int, Problem) ⇒ Unit) extends Actor {

  implicit val execCtx = context.dispatcher
  private object Retry
  private object RetrySucceeded
  private object RetryFinallyFailed

  def handleFailedAttempt(started: Deadline, attempt: Int)(problem: Problem) {
    val isLoopFinalFailure =
      settings match {
        case TimeLimitedRetrySettings(_, limit, _) ⇒
          started.lapExceeds(limit)
        case AttemptLimitedRetrySettings(_, limit, _) ⇒
          attempt >= limit
      }

    if (isLoopFinalFailure) {
      settings.infiniteLoopPause match {
        case None ⇒
          onFinalFailure(started.lap, attempt, problem)
          self ! RetryFinallyFailed
        case Some(pause) ⇒
          onFailedLoop(started.lap, attempt, problem)
          context.system.scheduler.scheduleOnce(pause, self, Retry)
      }
    } else {
      onFailedAttempt(started.lap, attempt, problem)
      context.system.scheduler.scheduleOnce(settings.pause, self, Retry)

    }
  }

  def receiveRetry(started: Deadline, attempt: Int): Receive = {
    case Retry ⇒
      toTry().onComplete(
        handleFailedAttempt(started, attempt),
        result ⇒ {
          onSuccess(result)
          self ! RetrySucceeded
        })
      context.become(receiveRetry(started, attempt + 1))

    case RetrySucceeded ⇒
      context.stop(self)

    case RetryFinallyFailed ⇒
      context.stop(self)

  }

  def receive: Receive = receiveRetry(Deadline.now, 1)

  override def preStart() {
    self ! Retry
  }

}



