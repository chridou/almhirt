package almhirt.akkax

import akka.actor.Actor
import almhirt.common._
import almhirt.context.HasAlmhirtContext
import almhirt.herder.HerderMessages
import almhirt.problem.ProblemCause


trait AlmActor extends Actor with HasAlmhirtContext with AlmActorSupport {

  private object DefaultComponentIdProvider extends ActorComponentIdProvider {
    def componentId = ComponentId(AppName("almhirt"), ComponentName(self.path.name))
  }

  implicit def componentNameProvider: ActorComponentIdProvider = DefaultComponentIdProvider

  def registerCircuitControl(curcuitControl: CircuitControl)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.CircuitMessages.RegisterCircuitControl(cnp.componentId, curcuitControl))

  def deregisterCircuitControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessages.CircuitMessages.DeregisterCircuitControl(cnp.componentId))

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
}