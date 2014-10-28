package almhirt.herder.herdingdogs

import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.herder.HerderMessages
import akka.actor.ActorRef
import almhirt.akkax.{ CircuitControl, ComponentId }

object CircuitsHerdingDog {

  val actorname = "circuits-herdingdog"
}

private[almhirt] class CircuitsHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {
  import HerderMessages.CircuitMessages._

  implicit val executor = almhirtContext.futuresContext

  var circuitControls: Map[ComponentId, CircuitControl] = Map.empty

  def receiveRunning: Receive = {
    case RegisterCircuitControl(ownerId, cb) =>
      log.info(s"""Circuit control registered for "${ownerId}".""")
      circuitControls = circuitControls + (ownerId -> cb)

    case DeregisterCircuitControl(ownerId) =>
      log.info(s"""Circuit control deregistered for "${ownerId}".""")
      circuitControls = circuitControls - ownerId

    case ReportCircuitStates =>
      val pinnedSender = sender()
      val futs = circuitControls.map({ case (ownerId, cb) => cb.state.map(st => (ownerId, st)) })
      val statesF = AlmFuture.sequence(futs.toSeq)
      statesF.onComplete(
        fail => log.error(s"Could not determine circuit states:\n$fail"),
        states => pinnedSender ! CircuitStates(states.toSeq.sortBy(_._1)))

    case AttemptCloseCircuit(ownerId) =>
      circuitControls.find(_._1 == ownerId) match {
        case Some(cc) =>
          cc._2.attemptClose
          log.info(s"""Sent close request to circuit control "$ownerId".""")
        case None => log.warning(s"""There is no circuit control named "$ownerId".""")
      }

    case RemoveFuseFromCircuit(ownerId) =>
      circuitControls.find(_._1 == ownerId) match {
        case Some(cc) =>
          cc._2.removeFuse
          log.info(s"""Sent remove fuse request to circuit control "$ownerId".""")
        case None => log.warning(s"""There is no circuit control named "$ownerId".""")
      }

    case DestroyCircuit(ownerId) =>
      circuitControls.find(_._1 == ownerId) match {
        case Some(cc) =>
          cc._2.destroy
          log.info(s"""Sent destroy request to circuit control "$ownerId".""")
        case None => log.warning(s"""There is no circuit control named "$ownerId".""")
      }

    case CircumventCircuit(ownerId) =>
      circuitControls.find(_._1 == ownerId) match {
        case Some(cc) =>
          cc._2.circumvent
          log.info(s"""Sent circumvent request to circuit control "$ownerId".""")
        case None => log.warning(s"""There is no circuit control named "$ownerId".""")
      }
  
  }

  override def receive: Receive = receiveRunning
} 