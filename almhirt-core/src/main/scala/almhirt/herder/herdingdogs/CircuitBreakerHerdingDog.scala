package almhirt.herder.herdingdogs

import akka.actor._
import almhirt.context._
import almhirt.herder.HerderMessage
import akka.actor.ActorRef
import almhirt.akkax.AlmCircuitBreaker

object CircuitBreakerHerdingDog {

  val actorname = "circuit-breaker-herdingdog"
}

private[almhirt] class CircuitBreakerHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {

  var circuitBreakers: Map[ActorRef, AlmCircuitBreaker] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.RegisterCircuitBreaker(owner, cb) =>
      log.info(s"""Circuit breaker registered for "${owner.path.name}".""")
      circuitBreakers = circuitBreakers + (owner -> cb)

    case HerderMessage.DeregisterCircuitBreaker(owner) =>
      log.info(s"""Circuit breaker deregistered for "${owner.path.name}".""")
      circuitBreakers = circuitBreakers - owner

    case HerderMessage.ReportCircuitBreakerStates =>
      sender() ! HerderMessage.CircuitBreakerStates(circuitBreakers.map({ case (owner, cb) => (owner.path.name, cb.state) }))
      
    case HerderMessage.AttemptCloseCircuitBreaker(name) =>
      circuitBreakers.find(_._1.path.name == name).foreach(_._2.attemptClose)
      
    case HerderMessage.RemoveFuseFromCircuitBreaker(name) =>
      circuitBreakers.find(_._1.path.name == name).foreach(_._2.removeFuse)

    case HerderMessage.DestroyFuseInCircuitBreaker(name) =>
      circuitBreakers.find(_._1.path.name == name).foreach(_._2.destroyFuse)
  }

  override def receive: Receive = receiveRunning

} 