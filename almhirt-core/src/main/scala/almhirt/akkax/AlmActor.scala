package almhirt.akkax

import scala.language.implicitConversions 
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
}