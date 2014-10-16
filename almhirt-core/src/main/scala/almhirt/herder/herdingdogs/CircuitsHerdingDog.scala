package almhirt.herder.herdingdogs

import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.herder.HerderMessage
import akka.actor.ActorRef
import almhirt.akkax.{ CircuitControl, ComponentId }

object CircuitsHerdingDog {

  val actorname = "circuits-herdingdog"
}

private[almhirt] class CircuitsHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {

  implicit val executor = almhirtContext.futuresContext

  var circuitControls: Map[ComponentId, CircuitControl] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.RegisterCircuitControl(ownerId, cb) =>
      log.info(s"""Circuit control registered for "${ownerId}".""")
      circuitControls = circuitControls + (ownerId -> cb)

    case HerderMessage.DeregisterCircuitControl(ownerId) =>
      log.info(s"""Circuit control deregistered for "${ownerId}".""")
      circuitControls = circuitControls - ownerId

    case HerderMessage.ReportCircuitStates =>
      val pinnedSender = sender()
      val futs = circuitControls.map({ case (ownerId, cb) => cb.state.map(st => (ownerId, st)) })
      val statesF = AlmFuture.sequence(futs.toSeq)
      statesF.onComplete(
        fail => log.error(s"Could not determine circuit states:\n$fail"),
        states => pinnedSender ! HerderMessage.CircuitStates(states.toSeq.sortBy(_._1)))

    case HerderMessage.AttemptCloseCircuit(ownerId) =>
      circuitControls.find(_._1 == ownerId) match {
        case Some(cc) =>
          cc._2.attemptClose
          log.info(s"""Sent close request to circuit control "$ownerId".""")
        case None => log.warning(s"""There is no circuit control named "$ownerId".""")
      }

    case HerderMessage.RemoveFuseFromCircuit(ownerId) =>
      circuitControls.find(_._1 == ownerId) match {
        case Some(cc) =>
          cc._2.removeFuse
          log.info(s"""Sent remove fuse request to circuit control "$ownerId".""")
        case None => log.warning(s"""There is no circuit control named "$ownerId".""")
      }

    case HerderMessage.DestroyFuseInCircuit(ownerId) =>
      circuitControls.find(_._1 == ownerId) match {
        case Some(cc) =>
          cc._2.destroyFuse
          log.info(s"""Sent destroy fuse request to circuit control "$ownerId".""")
        case None => log.warning(s"""There is no circuit control named "$ownerId".""")
      }
  }

  override def receive: Receive = receiveRunning
} 