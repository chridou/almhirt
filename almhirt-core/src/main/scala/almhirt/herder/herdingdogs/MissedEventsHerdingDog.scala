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

  implicit object tOrdering extends scala.math.Ordering[(String, almhirt.problem.Severity, Int)] {
    def compare(a: (String, almhirt.problem.Severity, Int), b: (String, almhirt.problem.Severity, Int)): Int =
      if ((a._2 compare b._2) == 0) {
        if ((a._3 compare b._3) == 0) {
          a._1 compare b._1
        } else {
          b._3 compare a._3
        }
      } else {
        b._2 compare a._2
      }
  }

  var missedEvents: Map[String, (almhirt.problem.Severity, Int)] = Map.empty

  def receiveRunning: Receive = {
    case HerderMessage.MissedEvent(name, event, severity, problem, timestamp) =>
      missedEvents = missedEvents get name match {
        case None =>
          log.info(s"""First missing event notification from "$name" with severity $severity.""")
          missedEvents + (name -> (severity -> 1))
        case Some((oldSeverity, count)) =>
          if (severity > oldSeverity) {
            log.info(s"Severity increased for $name from $oldSeverity to $severity.")
          }
          val newSeverity = oldSeverity and severity

          missedEvents + (name -> (newSeverity -> (count + 1)))
      }

    case HerderMessage.ReportMissedEvents =>
      val missed = missedEvents.map { case (owner, (severity, count)) => (owner, severity, count) }.toSeq.sorted
      sender() ! HerderMessage.MissedEvents(missed)
  }

  override def receive: Receive = receiveRunning
} 