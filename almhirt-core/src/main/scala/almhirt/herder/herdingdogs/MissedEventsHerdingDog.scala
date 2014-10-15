package almhirt.herder.herdingdogs

import akka.actor._
import almhirt.common._
import almhirt.context._
import almhirt.akkax.ComponentId
import almhirt.herder.HerderMessage
import akka.actor.ActorRef

object MissedEventsHerdingDog {
  val actorname = "missed-events-herdingdog"

}

private[almhirt] class MissedEventsHerdingDog()(implicit override val almhirtContext: AlmhirtContext) extends Actor with HasAlmhirtContext with ActorLogging {

  implicit val executor = almhirtContext.futuresContext

  implicit object tOrdering extends scala.math.Ordering[(ComponentId, almhirt.problem.Severity, Int)] {
    def compare(a: (ComponentId, almhirt.problem.Severity, Int), b: (ComponentId, almhirt.problem.Severity, Int)): Int =
      if (a._1.app == b._1.app) {
        if (a._2 == b._2) {
          if (a._3 == b._3) {
            a._1.component compare b._1.component
          } else {
            b._3 compare a._3
          }
        } else {
          b._2 compare a._2
        }
      } else {
        a._1.app compare b._1.app
      }
  }

  var missedEvents: Map[ComponentId, (almhirt.problem.Severity, Int)] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.MissedEvent(componentId, event, severity, problem, timestamp) =>
      missedEvents = missedEvents get componentId match {
        case None =>
          log.info(s"""First missing event notification from "$componentId" with severity $severity.""")
          missedEvents + (componentId -> (severity -> 1))
        case Some((oldSeverity, count)) =>
          if (severity > oldSeverity) {
            log.info(s"Severity increased for $componentId from $oldSeverity to $severity.")
          }
          val newSeverity = oldSeverity and severity

          missedEvents + (componentId -> (newSeverity -> (count + 1)))
      }

    case HerderMessage.ReportMissedEvents =>
      val missed = missedEvents.map { case (componentId, (severity, count)) => (componentId, severity, count) }.toSeq.sorted
      sender() ! HerderMessage.MissedEvents(missed)
  }

  override def receive: Receive = receiveRunning
} 