package almhirt.herder.herdingdogs

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.herder.HerderMessages
import akka.actor.ActorRef
import almhirt.akkax.{ ComponentControl, ComponentId }

object ComponentControlHerdingDog {

  val actorname = "component-control-herdingdog"
}

private[almhirt] class ComponentControlHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {
  import HerderMessages.ComponentControlMessages._

  implicit val executor = almhirtContext.futuresContext

  var componentControls: Map[ComponentId, ComponentControl] = Map.empty

  def receiveRunning: Receive = {
    case RegisterComponentControl(ownerId, cb) ⇒
      log.info(s"""Component control registered for "${ownerId}".""")
      componentControls = componentControls + (ownerId → cb)

    case DeregisterComponentControl(ownerId) ⇒
      log.info(s"""Component control deregistered for "${ownerId}".""")
      componentControls = componentControls - ownerId

    case ReportComponentStates ⇒
      val pinnedSender = sender()
      val futs = componentControls.map({
        case (ownerId, cb) ⇒
          cb.state(1.second).recover(p ⇒ almhirt.akkax.ComponentState.Error(p)).map(st ⇒ (ownerId, st))
      })
      val statesF = AlmFuture.sequence(futs.toSeq)
      statesF.onComplete(
        fail ⇒ log.error(s"Could not determine circuit states:\n$fail"),
        states ⇒ pinnedSender ! ComponentStates(states.toSeq.sortBy(_._1)))

    case AttemptComponentControlAction(ownerId, action) ⇒
      componentControls.find(_._1 == ownerId) match {
        case Some(cc) ⇒
          if (cc._2.supports(action)) {
            cc._2.changeState(action)
            log.info(s"""Sent request for action $action to component "$ownerId".""")
          } else {
            log.warning(s""""$ownerId" does not support $action.""")
          }
        case None ⇒ log.warning(s"""There is no component named "$ownerId".""")
      }
  }

  override def receive: Receive = receiveRunning
} 