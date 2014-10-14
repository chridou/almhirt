package almhirt.context

import akka.actor.ActorRef
import almhirt.common._
import almhirt.akkax.CircuitControl
import almhirt.herder.HerderMessage

trait HasAlmhirtContext {
  implicit def almhirtContext: AlmhirtContext

  implicit class AlmhirtContextOps(self: AlmhirtContext) {
    def registerCircuitControl(name: String, curcuitControl: CircuitControl) =
      self.tellHerder(HerderMessage.RegisterCircuitControl(name, curcuitControl))

    def deregisterCircuitControl(name: String) =
      self.tellHerder(HerderMessage.DeregisterCircuitControl(name))

    def reportMissedEvent(name: String, event: Event, severity: almhirt.problem.Severity, problem: Problem) =
      self.tellHerder(HerderMessage.MissedEvent(name, event, MajorSeverity, problem, almhirtContext.getUtcTimestamp))
  }
}