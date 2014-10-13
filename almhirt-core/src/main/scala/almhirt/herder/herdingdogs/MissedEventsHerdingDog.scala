package almhirt.herder.herdingdogs

import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.herder.HerderMessage
import akka.actor.ActorRef

object MissedEventsHerdingDog {
  val actorname = "missed-events-herdingdog"

}

private[almhirt] class MissedEventsHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {

  implicit val executor = almhirtContext.futuresContext

  var missedEvents: Map[ActorRef, (almhirt.problem.Severity, Int)] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.MissedEvent(event, severity, problem, timestamp) =>
      missedEvents = missedEvents get sender() match {
        case None =>
          log.info(s"""First missing event notification from "${sender().path.name}".""")
          missedEvents + (sender() -> (severity -> 1))
        case Some((oldSeverity, count)) =>
          val newSeverity = oldSeverity and severity

          missedEvents + (sender() -> (newSeverity -> (count + 1)))
      }

    case HerderMessage.ReportMissedEvents =>
      sender() ! HerderMessage.MissedEvents(missedEvents.map { case (act, item) => (act.path.name, item) })
  }

  override def receive: Receive = receiveRunning
} 