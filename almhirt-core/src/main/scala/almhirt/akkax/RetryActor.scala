package almhirt.akkax

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.configuration.{ RetrySettings, TimeLimitedRetrySettings, AttemptLimitedRetrySettings }

object RetryActor {
  def props[T](
    settings: RetrySettings,
    toTry: () => AlmFuture[T],
    onSuccess: T => Unit,
    onFailedAttempt: (FiniteDuration, Int, Problem) => Unit,
    onFinalFailure: (FiniteDuration, Int, Problem) => Unit): Props =
    Props(new RetryActorImpl(settings, toTry, onSuccess, onFailedAttempt, onFinalFailure))
}

private[almhirt] class RetryActorImpl[T](
  settings: RetrySettings,
  toTry: () => AlmFuture[T],
  onSuccess: T => Unit,
  onFailedAttempt: (FiniteDuration, Int, Problem) => Unit,
  onFinalFailure: (FiniteDuration, Int, Problem) => Unit) extends Actor {

  implicit val execCtx = context.dispatcher
  private object Retry
  private object RetrySucceeded
  private object RetryFinallyFailed

  def handleFailedAttempt(started: Deadline, attempt: Int)(problem: Problem) {
    val isFinalFailure =
      settings match {
        case TimeLimitedRetrySettings(_, limit) =>
          started.lapExceeds(limit)
        case AttemptLimitedRetrySettings(_, limit) =>
          attempt >= limit
      }

    if (isFinalFailure) {
      onFinalFailure(started.lap, attempt, problem)
      self ! RetryFinallyFailed
    } else {
      onFailedAttempt(started.lap, attempt, problem)
      context.system.scheduler.scheduleOnce(settings.pause, self, Retry)

    }
  }

  def receiveRetry(started: Deadline, attempt: Int): Receive = {
    case Retry =>
      toTry().onComplete(
        handleFailedAttempt(started, attempt),
        result => {
          onSuccess(result)
          self ! RetrySucceeded
        })
      context.become(receiveRetry(started, attempt + 1))

    case RetrySucceeded =>
      context.stop(self)

    case RetryFinallyFailed =>
      context.stop(self)

  }

  def receive: Receive = receiveRetry(Deadline.now, 1)

  override def preStart() {
    self ! Retry
  }

}



