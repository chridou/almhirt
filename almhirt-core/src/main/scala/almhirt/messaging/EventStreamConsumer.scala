package almhirt.messaging

import akka.actor._
import akka.stream.actor.ActorProducer
import almhirt.common._

object EventStreamConsumer {
  sealed trait EventStreamConsumerMessage
  /** Signal that you are interested in dispatching n events */
  final case class DemandEventConsumption(amount: Int) extends EventStreamConsumerMessage

  /** When receiving this message, the specified number of events must be sent! */
  final case class SendEventsNow(amount: Int) extends EventStreamConsumerMessage

  /** Send the amount of events ordered by the consumer via SendEventsNow */
  final case class ConsumeEvents(events: Seq[Event]) extends EventStreamConsumerMessage
  sealed trait ConsumeEventsResponse extends EventStreamConsumerMessage
  final case class EventsConsumed(amount: Int) extends ConsumeEventsResponse
  final case class EventsNotConsumed(problem: Problem) extends ConsumeEventsResponse

}

private[messaging] class EventStreamConsumer() extends ActorProducer[Event] with ActorLogging {
  import ActorProducer._
  import EventStreamConsumer._

  var demands: Vector[ActorRef] = Vector.empty

  def addDemand(demander: ActorRef, amount: Int) {
    val newDemand = Vector.fill(amount)(demander)
    demands = demands ++ newDemand
  }

  private def collecting: Receive = {
    case DemandEventConsumption(amount) =>
      addDemand(sender(), amount)
      if (!demands.isEmpty && totalDemand > 0)
        requestEvents()
        
    case ConsumeEvents(events) =>
      sender() ! EventsNotConsumed(UnspecifiedProblem("You have not been asked to send events!"))
      
    case Request(amount) =>
      if (!demands.isEmpty && totalDemand > 0)
        requestEvents()
  }

  private def dispatching(toCollect: Int): Receive = {
    case DemandEventConsumption(amount) =>
      addDemand(sender(), amount)
      if (toCollect == 0 && !demands.isEmpty && totalDemand > 0)
        requestEvents()

    case ConsumeEvents(events) =>
      events.foreach(onNext)
      if (toCollect == 0 && !demands.isEmpty && totalDemand > 0)
        requestEvents()
      else
        context.become(collecting)
        
    case Request(amount) =>
      ()

  }

  def receive: Receive = collecting

  private def requestEvents() {
    val toRequest =
      demands.take(totalDemand)
        .groupBy(d => d)
        .map { case (demander, demands) => (demander, demands.size) }
        .toMap
    val numExpected = toRequest.map(_._2).sum
    toRequest.foreach { case (demander, demand) => demander ! SendEventsNow(demand) }
    context.become(dispatching(numExpected))
  }
}
