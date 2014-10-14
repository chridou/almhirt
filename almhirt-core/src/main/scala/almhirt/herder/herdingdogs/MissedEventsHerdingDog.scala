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

  var missedEvents: Map[String, (almhirt.problem.Severity, Int)] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.MissedEvent(name, event, severity, problem, timestamp) =>
      missedEvents = missedEvents get name match {
        case None =>
          log.info(s"""First missing event notification from "$name".""")
          missedEvents + (name -> (severity -> 1))
        case Some((oldSeverity, count)) =>
          val newSeverity = oldSeverity and severity

          missedEvents + (name -> (newSeverity -> (count + 1)))
      }

    case HerderMessage.ReportMissedEvents =>
      sender() ! HerderMessage.MissedEvents(missedEvents.map { case (owner, item) => (owner, item) })
  }

  override def receive: Receive = receiveRunning
} 