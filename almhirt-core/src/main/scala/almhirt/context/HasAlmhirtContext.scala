package almhirt.context

import akka.actor.ActorRef
import almhirt.common._
import almhirt.akkax.{ CircuitControl, ComponentId }
import almhirt.herder.HerderMessage

trait HasAlmhirtContext {
  implicit def almhirtContext: AlmhirtContext

  implicit class AlmhirtContextOps(self: AlmhirtContext) {
    def registerCircuitControl(id: ComponentId, curcuitControl: CircuitControl) =
      self.tellHerder(HerderMessage.RegisterCircuitControl(id, curcuitControl))

    def deregisterCircuitControl(id: ComponentId) =
      self.tellHerder(HerderMessage.DeregisterCircuitControl(id))

    def reportMissedEvent(id: ComponentId, event: Event, severity: almhirt.problem.Severity, problem: Problem) =
      self.tellHerder(HerderMessage.MissedEvent(id, event, severity, problem, almhirtContext.getUtcTimestamp))
  }
}