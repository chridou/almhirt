package almhirt.corex.spray.eventlog

import almhirt.common._
import almhirt.core.Almhirt
import almhirt.corex.spray.SingleTypeHttpPublisher
import scala.concurrent.duration.FiniteDuration
import akka.actor.ActorRef

abstract class RespondingEventPublisher()(implicit myAlmhirt: Almhirt) extends HttpEventPublisher() {
  import RespondingEventPublisher._

  override def onProblem(event: Event, problem: Problem, respondTo: ActorRef) {
    respondTo ! EventPossiblyNotLogged(event, problem)
  }

  override def onSuccess(event: Event, time: FiniteDuration, respondTo: ActorRef) {
    respondTo ! EventLogged(event)
  }

}

object RespondingEventPublisher {
  sealed trait RespondingEventPublisherResponse
  final case class EventLogged(event: Event) extends RespondingEventPublisherResponse
  final case class EventPossiblyNotLogged(event: Event, problem: Problem) extends RespondingEventPublisherResponse
}