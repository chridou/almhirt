package almhirt.herder.herdingdogs

import akka.actor.Actor
import almhirt.context._
import almhirt.herder.HerderMessage
import akka.actor.ActorRef
import almhirt.akkax.AlmCircuitBreaker

object CircuitBreakerHerdingDog {

  val actorname = "circuit-breaker-herdingdog"
}

private[almhirt] class CircuitBreakerHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext {

  var circuitBreakers: Map[ActorRef, AlmCircuitBreaker] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.RegisterCircuitBreaker(owner, cb) =>
      circuitBreakers = circuitBreakers + (owner -> cb)

    case HerderMessage.DeregisterCircuitBreaker(owner) =>
      circuitBreakers = circuitBreakers - owner

    case HerderMessage.ReportCircuitBreakerStates =>
      sender() ! HerderMessage.CircuitBreakerStates(circuitBreakers.map({ case (owner, cb) => (owner.path.name, cb.state) }))
      
    case HerderMessage.ResetCircuitBreaker(name) =>
      circuitBreakers.find(_._1.path.name == name).foreach(_._2.reset)
      
    case HerderMessage.RemoveFuseFromCircuitBreaker(name) =>
      circuitBreakers.find(_._1.path.name == name).foreach(_._2.removeFuse)
      
    case HerderMessage.DestroyFuseInCircuitBreaker(name) =>
      circuitBreakers.find(_._1.path.name == name).foreach(_._2.destroyFuse)
  }

  override def receive: Receive = receiveRunning

} 