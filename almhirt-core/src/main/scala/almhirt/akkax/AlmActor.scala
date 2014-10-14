package almhirt.akkax

import akka.actor.Actor
import almhirt.common._
import almhirt.context.HasAlmhirtContext
import almhirt.herder.HerderMessage

trait AlmActor extends Actor with HasAlmhirtContext with AlmActorSupport {
  trait ActorComponentNameProvider {
    def componentName: String
  }
  
  private object DefaultComponentNameProvider extends ActorComponentNameProvider {
    def componentName = self.path.name
  }

  implicit def componentNameProvider: ActorComponentNameProvider = DefaultComponentNameProvider

  def registerCircuitControl(curcuitControl: CircuitControl)(implicit cnp: ActorComponentNameProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.RegisterCircuitControl(cnp.componentName, curcuitControl))

  def deregisterCircuitControl()(implicit cnp: ActorComponentNameProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.DeregisterCircuitControl(cnp.componentName))

  def reportMissedEvent(event: Event, severity: almhirt.problem.Severity, problem: Problem)(implicit cnp: ActorComponentNameProvider): Unit =
    almhirtContext.tellHerder(HerderMessage.MissedEvent(cnp.componentName, event, MajorSeverity, problem, almhirtContext.getUtcTimestamp))
}