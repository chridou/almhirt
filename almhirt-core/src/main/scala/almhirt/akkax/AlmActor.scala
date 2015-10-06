package almhirt.akkax

import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.syntax.validation._
import akka.actor._
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

//  implicit def CommandToCommandRepresentation(cmd: Command): CommandRepresentation = CommandRepresentation.FullCommand(cmd)
//  implicit def CommandIdToCommandRepresentation(id: CommandId): CommandRepresentation = CommandRepresentation.CommandIdOnly(id)

  protected val born = java.time.ZonedDateTime.now()
  protected val bornUtc = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
  // protected final def livingFor:  = this.almhirtContext.getUtcTimestamp

  def selectExecutionContext(implicit selector: ExtendedExecutionContextSelector): ExecutionContext =
    selector.select(this.almhirtContext, this.context)

  def registerCircuitControl(circuitControl: CircuitControl)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.CircuitMessages.RegisterCircuitControl(cnp.componentId, circuitControl))

  def deregisterCircuitControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.CircuitMessages.DeregisterCircuitControl(cnp.componentId))

  def reportRejectedCommand(command: Command, severity: almhirt.problem.Severity, cause: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit = {
    val timestamp = almhirtContext.getUtcTimestamp
    val repr = CommandRepresentation.FullCommand(command).downgradeToIdAndType
    almhirtContext.tellHerder(HerderMessages.CommandMessages.RejectedCommand(cnp.componentId, repr, severity, cause, timestamp))
    almhirtContext.fireNonStreamEvent(events.CommandRejected(repr, severity)(EventHeader(EventId(almhirtContext.getUniqueString()), timestamp), cnp.componentId))
  }

  def reportMissedEvent(event: Event, severity: almhirt.problem.Severity, cause: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit = {
    val timestamp = almhirtContext.getUtcTimestamp
    almhirtContext.tellHerder(HerderMessages.EventMessages.MissedEvent(cnp.componentId, event, severity, cause, timestamp))
    almhirtContext.fireNonStreamEvent(events.EventNotProcessed(event.eventId, event.getClass.getName, severity)(EventHeader(EventId(almhirtContext.getUniqueString()), timestamp), cnp.componentId))
  }

  def reportFailure(cause: ProblemCause, severity: almhirt.problem.Severity)(implicit cnp: ActorComponentIdProvider): Unit = {
    val timestamp = almhirtContext.getUtcTimestamp
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(cnp.componentId, cause, severity, timestamp))
    almhirtContext.fireNonStreamEvent(events.FailureReported(cause.toProblem, severity)(EventHeader(EventId(almhirtContext.getUniqueString()), timestamp), cnp.componentId))
  }

  def reportMinorFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit = {
    val timestamp = almhirtContext.getUtcTimestamp
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(cnp.componentId, failure, MinorSeverity, timestamp))
    almhirtContext.fireNonStreamEvent(events.FailureReported(failure.toProblem, MinorSeverity)(EventHeader(EventId(almhirtContext.getUniqueString()), timestamp), cnp.componentId))
  }

  def reportMajorFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit = {
    val timestamp = almhirtContext.getUtcTimestamp
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(cnp.componentId, failure, MajorSeverity, timestamp))
    almhirtContext.fireNonStreamEvent(events.FailureReported(failure.toProblem, MajorSeverity)(EventHeader(EventId(almhirtContext.getUniqueString()), timestamp), cnp.componentId))
  }

  def reportCriticalFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit = {
    val timestamp = almhirtContext.getUtcTimestamp
    almhirtContext.tellHerder(HerderMessages.FailureMessages.FailureOccured(cnp.componentId, failure, CriticalSeverity, timestamp))
    almhirtContext.fireNonStreamEvent(events.FailureReported(failure.toProblem, CriticalSeverity)(EventHeader(EventId(almhirtContext.getUniqueString()), timestamp), cnp.componentId))
  }

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

  def retryFuture[T](policy: RetryPolicyExt)(f: ⇒ AlmFuture[T]): AlmFuture[T] = {
    import almhirt.configuration.RetryPolicy
    val executor = policy.executorSelector.map { selectExecutionContext(_) } getOrElse this.context.dispatcher
    val retrySettings = RetryPolicy(policy.numberOfRetries, policy.delay)
    val retryNotification: Option[(almhirt.configuration.NumberOfRetries, scala.concurrent.duration.FiniteDuration, Problem) ⇒ Unit] =
      policy.notifiyingParams.map {
        case RetryPolicyExt.NotifyingParams(importance, Some(contextDesc)) ⇒
          (nor, nextIn, lastProblem) ⇒ inform(s"""  |$contextDesc
                                       |Last problem message: ${lastProblem.message}
                                       |Last problem type: ${lastProblem.problemType}
                                       |Retries left: $nor
                                       |Next retry in: ${nextIn.defaultUnitString}""".stripMargin, importance)
          case RetryPolicyExt.NotifyingParams(importance, None) ⇒
          (nor, nextIn, lastProblem) ⇒ inform(s"""|Last problem message: ${lastProblem.message}
                                       |Last problem type: ${lastProblem.problemType}
                                       |Retries left: $nor
                                       |Next retry in: ${nextIn.defaultUnitString}""".stripMargin, importance)
      }

    AlmFuture.retryScaffolding(f, retrySettings, executor, this.context.system.scheduler, retryNotification)
  }

  def retryAsk[T](policy: RetryPolicyExt)(actor: ActorRef, m: Any, atMost: FiniteDuration)(implicit classTag: ClassTag[T]): AlmFuture[T] = {
    import akka.pattern._
    import almhirt.almfuture.all._
    implicit val executor = policy.executorSelector.map { selectExecutionContext(_) } getOrElse this.context.dispatcher
    val f: AlmFuture[Any] = retryFuture(policy) {
      (actor ? m)(atMost).toAlmFuture
    }
    f.mapCast[T]
  }

  def retryAskEvalForFailure[T](policy: RetryPolicyExt)(failPf: PartialFunction[T, Problem])(actor: ActorRef, m: Any, atMost: FiniteDuration)(implicit classTag: ClassTag[T]): AlmFuture[T] = {
    import akka.pattern._
    import almhirt.almfuture.all._
    implicit val executor = policy.executorSelector.map { selectExecutionContext(_) } getOrElse this.context.dispatcher
    retryFuture(policy) {
      (actor ? m)(atMost).mapCastTo[T].mapV { res ⇒
        if (failPf.isDefinedAt(res))
          failPf(res).failure
        else
          res.success
      }
    }
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