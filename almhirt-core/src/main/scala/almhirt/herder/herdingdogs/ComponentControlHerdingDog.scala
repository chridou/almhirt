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
      val futs = componentControls.map({ case (ownerId, cb) ⇒ cb.state(1.second).map(st ⇒ (ownerId, st)) })
      val statesF = AlmFuture.sequence(futs.toSeq)
      statesF.onComplete(
        fail ⇒ log.error(s"Could not determine circuit states:\n$fail"),
        states ⇒ pinnedSender ! ComponentStates(states.toSeq.sortBy(_._1)))

    case AttemptPause(ownerId) ⇒
      componentControls.find(_._1 == ownerId) match {
        case Some(cc) ⇒
          cc._2.pause()
          log.info(s"""Sent pause request to component "$ownerId".""")
        case None ⇒ log.warning(s"""There is no component named "$ownerId".""")
      }

    case AttemptResume(ownerId) ⇒
      componentControls.find(_._1 == ownerId) match {
        case Some(cc) ⇒
          cc._2.resume()
          log.info(s"""Sent resume fuse request to component "$ownerId".""")
        case None ⇒ log.warning(s"""There is no component named "$ownerId".""")
      }

    case AttemptRestart(ownerId) ⇒
      componentControls.find(_._1 == ownerId) match {
        case Some(cc) ⇒
          if (cc._2.supportsRestart) {
            cc._2.restart()
            log.info(s"""Sent restart fuse request to component "$ownerId".""")
          } else {
            log.info(s"""Component "$ownerId" does not support restart.""")
          }
        case None ⇒ log.warning(s"""There is no component named "$ownerId".""")
      }
  }

  override def receive: Receive = receiveRunning
} 