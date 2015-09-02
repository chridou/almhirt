package almhirt.herder.herdingdogs

import scala.concurrent.duration._
import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.herder.HerderMessages
import akka.actor.ActorRef
import almhirt.akkax.{ ComponentControl, ComponentId }
import almhirt.akkax._

object ComponentControlHerdingDog {

  val actorname = "component-control-herdingdog"
}

private[almhirt] class ComponentControlHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends AlmActor with HasAlmhirtContext with AlmActorLogging {
  import HerderMessages.ComponentControlMessages._

  implicit val executor = almhirtContext.futuresContext

  var componentControls: Map[ComponentId, ComponentControl] = Map.empty

  def receiveRunning: Receive = {
    case RegisterComponentControl(ownerId, cb) ⇒
      if (cb == null) {
        logError(s"ComponentControl for $ownerId is null!")
      } else {
        logInfo(s"""Component control registered for "${ownerId}".""")
        componentControls = componentControls + (ownerId → cb)
      }

    case DeregisterComponentControl(ownerId) ⇒
      logInfo(s"""Component control deregistered for "${ownerId}".""")
      componentControls = componentControls - ownerId

    case ReportComponentStates ⇒
      val pinnedSender = sender()
      val futs = componentControls.map({
        case (ownerId, cb) ⇒
          try {
            cb.state(1.second).recover(p ⇒ almhirt.akkax.ComponentState.Error(p)).map(st ⇒ (ownerId, st))
          } catch {
            case scala.util.control.NonFatal(exn) ⇒
              logError(s"Failed to Report component state for $ownerId", exn)
              AlmFuture.successful((ownerId, almhirt.akkax.ComponentState.Error(UnspecifiedProblem(s"Failed to Report component state for $ownerId:\n$exn"))))
          }
      })
      val statesF = AlmFuture.sequence(futs.toSeq)
      statesF.onComplete(
        fail ⇒ logError(s"Could not determine circuit states:\n$fail"),
        states ⇒ pinnedSender ! ComponentStates(states.toSeq.sortBy(_._1)))

    case AttemptComponentControlAction(ownerId, action) ⇒
      componentControls.find(_._1 == ownerId) match {
        case Some(cc) ⇒
          if (cc._2.supports(action)) {
            cc._2.changeState(action)
            logInfo(s"""Sent request for action $action to component "$ownerId".""")
          } else {
            logWarning(s""""$ownerId" does not support $action.""")
          }
        case None ⇒ logWarning(s"""There is no component named "$ownerId".""")
      }
  }

  override def receive: Receive = receiveRunning

  override def preStart() {
    super.preStart()
    logInfo("Starting..")
  }

  override def postStop() {
    super.postStop()
    logInfo("Stopped..")
  }

} 