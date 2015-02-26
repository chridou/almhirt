package almhirt.akkax

import scala.language.implicitConversions
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.syntax.validation._
import akka.actor.Actor
import almhirt.common._
import almhirt.context.HasAlmhirtContext
import almhirt.herder.HerderMessages
import almhirt.problem.ProblemCause
import almhirt.tracking.CommandRepresentation

trait AlmActor extends Actor with HasAlmhirtContext with AlmActorSupport {

  private object DefaultComponentIdProvider extends ActorComponentIdProvider {
    def componentId = ComponentId(AppName("almhirt"), ComponentName(self.path.name))
  }

  implicit def componentNameProvider: ActorComponentIdProvider = DefaultComponentIdProvider

  implicit def CommandToCommandRepresentation(cmd: Command): CommandRepresentation = CommandRepresentation.FullCommand(cmd)
  implicit def CommandIdToCommandRepresentation(id: CommandId): CommandRepresentation = CommandRepresentation.CommandIdOnly(id)

  def selectExecutionContext(implicit selector: ExtendedExecutionContextSelector): ExecutionContext =
    selector.select(this.almhirtContext, this.context)

  def registerCircuitControl(circuitControl: CircuitControl)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.CircuitMessages.RegisterCircuitControl(cnp.componentId, circuitControl))

  def deregisterCircuitControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.CircuitMessages.DeregisterCircuitControl(cnp.componentId))

  def reportRejectedCommand(command: CommandRepresentation, severity: almhirt.problem.Severity, cause: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.CommandMessages.RejectedCommand(cnp.componentId, command, severity, cause, almhirtContext.getUtcTimestamp))

  def reportMissedEvent(event: Event, severity: almhirt.problem.Severity, cause: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.EventMessages.MissedEvent(cnp.componentId, event, severity, cause, almhirtContext.getUtcTimestamp))

  def reportFailure(cause: ProblemCause, severity: almhirt.problem.Severity)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(cnp.componentId, cause, severity, almhirtContext.getUtcTimestamp))

  def reportMinorFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(cnp.componentId, failure, MinorSeverity, almhirtContext.getUtcTimestamp))

  def reportMajorFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(cnp.componentId, failure, MajorSeverity, almhirtContext.getUtcTimestamp))

  def reportCriticalFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(cnp.componentId, failure, CriticalSeverity, almhirtContext.getUtcTimestamp))

  def inform(message: String, importance: Importance)(implicit cnp: ActorComponentIdProvider): Unit = {
    almhirtContext.tellHerder(HerderMessages.InformationMessages.Information(cnp.componentId, message, importance, almhirtContext.getUtcTimestamp))
  }

  def informNotWorthMentioning(message: String)(implicit cnp: ActorComponentIdProvider): Unit =
    inform(message, Importance.NotWorthMentioning)(cnp)

  def informMentionable(message: String)(implicit cnp: ActorComponentIdProvider): Unit =
    inform(message, Importance.Mentionable)(cnp)

  def informImportant(message: String)(implicit cnp: ActorComponentIdProvider): Unit =
    inform(message, Importance.Important)(cnp)

  def informVeryImportant(message: String)(implicit cnp: ActorComponentIdProvider): Unit =
    inform(message, Importance.VeryImportant)(cnp)

  def retryFuture[T](settings: XRetrySettings)(f: ⇒ AlmFuture[T]): AlmFuture[T] = {
    import almhirt.configuration.RetrySettings2
    val executor = settings.executorSelector.map { selectExecutionContext(_) } getOrElse this.context.dispatcher
    val retrySettings = RetrySettings2(settings.numberOfRetries, settings.delay)
    val retryNotification: Option[(almhirt.configuration.NumberOfRetries, scala.concurrent.duration.FiniteDuration) ⇒ Unit] =
      settings.notifiyingParams.map {
        case XRetrySettings.NotifyingParams(importance, Some(contextDesc)) ⇒
          (nor, nextIn) ⇒ inform(s"""  |$contextDesc
                                       |Retries left: $nor
                                       |Next retry in: ${nextIn.defaultUnitString}""".stripMargin, importance)
          case XRetrySettings.NotifyingParams(importance, None) ⇒
          (nor, nextIn) ⇒ inform(s"Retries left: $nor; Next retry in: ${nextIn.defaultUnitString}", importance)
      }

    AlmFuture.retryScaffolding(f, retrySettings, executor, this.context.system.scheduler, retryNotification)
  }

  @deprecated(message = "Use retryFuture", since = "0.7.6")
  def retryInforming[T](f: ⇒ AlmFuture[T])(numRetries: Int, retryDelay: FiniteDuration, importance: Importance = Importance.Mentionable, executor: ExecutionContext = this.context.dispatcher): AlmFuture[T] = {
    if (numRetries >= 0) {
      val p = Promise[AlmValidation[T]]

      f.onComplete(
        fail ⇒ innerRetryInforming(f, fail, p, numRetries, numRetries, retryDelay, importance, executor),
        succ ⇒ p.success(succ.success))(executor)

      new AlmFuture(p.future)
    } else {
      AlmFuture.failed(ArgumentProblem("numRetries must not be lower than zero!"))
    }
  }

    private def innerRetryInforming[T](f: ⇒ AlmFuture[T], lastFailure: Problem, promise: Promise[AlmValidation[T]], retriesLeft: Int, originalRetries: Int, retryDelay: FiniteDuration, importance: Importance, executor: ExecutionContext) {
    if (retriesLeft == 0) {
      inform(s"No retries left. Finally failed after $originalRetries retries.", importance)
      promise.success(lastFailure.failure)
    } else {
      inform(s"$retriesLeft of $originalRetries retries left.", importance)
      if (retryDelay == Duration.Zero) {
        f.onComplete(
          fail ⇒ innerRetryInforming(f, fail, promise, retriesLeft - 1, originalRetries, retryDelay, importance, executor),
          succ ⇒ promise.success(succ.success))(executor)
      } else {
        this.context.system.scheduler.scheduleOnce(retryDelay) {
          f.onComplete(
            fail ⇒ innerRetryInforming(f, fail, promise, retriesLeft - 1, originalRetries, retryDelay, importance, executor),
            succ ⇒ promise.success(succ.success))(executor)
        }(executor)
      }
    }
  }


}