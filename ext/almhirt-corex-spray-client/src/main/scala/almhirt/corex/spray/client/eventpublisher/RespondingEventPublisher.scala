package almhirt.corex.spray.client.eventpublisher

import almhirt.common._
import almhirt.core.Almhirt
import scala.concurrent.duration.FiniteDuration
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

abstract class RespondingEventPublisher()(implicit myAlmhirt: Almhirt) extends HttpEventPublisher() {
  import RespondingEventPublisher._

  override def onProblem(event: Event, problem: Problem, respondTo: ActorRef) {
    respondTo ! EventPossiblyNotPublished(event, problem)
  }

  override def onSuccess(event: Event, time: FiniteDuration, respondTo: ActorRef) {
    respondTo ! EventPublished(event)
  }

}

object RespondingEventPublisher {
  sealed trait RespondingEventPublisherResponse
  final case class EventPublished(event: Event) extends RespondingEventPublisherResponse
  final case class EventPossiblyNotPublished(event: Event, problem: Problem) extends RespondingEventPublisherResponse
}