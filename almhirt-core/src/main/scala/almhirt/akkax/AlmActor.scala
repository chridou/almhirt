package almhirt.akkax

import akka.actor.Actor
import almhirt.common._
import almhirt.context.HasAlmhirtContext
import almhirt.herder.HerderMessage
import almhirt.problem.ProblemCause


trait AlmActor extends Actor with HasAlmhirtContext with AlmActorSupport {

  private object DefaultComponentIdProvider extends ActorComponentIdProvider {
    def componentId = ComponentId(AppName("almhirt"), ComponentName(self.path.name))
  }

  implicit def componentNameProvider: ActorComponentIdProvider = DefaultComponentIdProvider

  def registerCircuitControl(curcuitControl: CircuitControl)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.RegisterCircuitControl(cnp.componentId, curcuitControl))

  def deregisterCircuitControl()(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.DeregisterCircuitControl(cnp.componentId))

  def reportMissedEvent(event: Event, severity: almhirt.problem.Severity, problem: Problem)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.MissedEvent(cnp.componentId, event, severity, problem, almhirtContext.getUtcTimestamp))

  def reportFailure(failure: ProblemCause, severity: almhirt.problem.Severity)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.FailureOccured(cnp.componentId, failure, severity, almhirtContext.getUtcTimestamp))

  def reportMinorFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.FailureOccured(cnp.componentId, failure, MinorSeverity, almhirtContext.getUtcTimestamp))

  def reportMajorFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.FailureOccured(cnp.componentId, failure, MajorSeverity, almhirtContext.getUtcTimestamp))

  def reportCriticalFailure(failure: ProblemCause)(implicit cnp: ActorComponentIdProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.FailureOccured(cnp.componentId, failure, CriticalSeverity, almhirtContext.getUtcTimestamp))
}