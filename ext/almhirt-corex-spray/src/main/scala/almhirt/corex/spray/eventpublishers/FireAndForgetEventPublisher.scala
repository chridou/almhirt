package almhirt.corex.spray.eventpublishers

import almhirt.common._
import almhirt.core.Almhirt
import scala.concurrent.duration.FiniteDuration
import akka.actor.ActorRef

abstract class FireAndForgetEventPublisher()(implicit myAlmhirt: Almhirt) extends HttpEventPublisher() {
  override def onProblem(event: Event, problem: Problem, respondTo: ActorRef) {
    log.warning(s"""Transmitting the event with id "${event.eventId} of type "${event.getClass().getName()}" failed: $problem""")
  }

  override def onSuccess(event: Event, time: FiniteDuration, respondTo: ActorRef) {}

}
