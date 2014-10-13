package almhirt.herder.herdingdogs

import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.herder.HerderMessage
import akka.actor.ActorRef
import almhirt.akkax.CircuitControl

object CircuitsHerdingDog {

  val actorname = "circuits-herdingdog"
}

private[almhirt] class CircuitsHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {

  implicit val executor = almhirtContext.futuresContext

  var circuitControls: Map[ActorRef, CircuitControl] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.RegisterCircuitControl(owner, cb) =>
      log.info(s"""Circuit control registered for "${owner.path.name}".""")
      circuitControls = circuitControls + (owner -> cb)

    case HerderMessage.DeregisterCircuitControl(owner) =>
      log.info(s"""Circuit control deregistered for "${owner.path.name}".""")
      circuitControls = circuitControls - owner

    case HerderMessage.ReportCircuitStates =>
      val pinnedSender = sender()
      val futs = circuitControls.map({ case (owner, cb) => cb.state.map(st => (owner.path.name, st)) })
      val statesF = AlmFuture.sequence(futs.toSeq)
      statesF.onComplete(
        fail => log.error(s"Could not determine circuit states:\n$fail"),
        states => pinnedSender ! HerderMessage.CircuitStates(states.toMap))

    case HerderMessage.AttemptCloseCircuit(name) =>
      circuitControls.find(_._1.path.name == name) match {
        case Some(cc) =>
          cc._2.attemptClose
          log.info(s"""Sent close request to circuit control "$name".""")
        case None => log.warning(s"""There is no circuit control named "$name".""")
      }

    case HerderMessage.RemoveFuseFromCircuit(name) =>
      circuitControls.find(_._1.path.name == name) match {
        case Some(cc) =>
          cc._2.removeFuse
          log.info(s"""Sent remove fuse request to circuit control "$name".""")
        case None => log.warning(s"""There is no circuit control named "$name".""")
      }

    case HerderMessage.DestroyFuseInCircuit(name) =>
      circuitControls.find(_._1.path.name == name) match {
        case Some(cc) =>
          cc._2.destroyFuse
          log.info(s"""Sent destroy fuse request to circuit control "$name".""")
        case None => log.warning(s"""There is no circuit control named "$name".""")
      }
  }

  override def receive: Receive = receiveRunning
} 