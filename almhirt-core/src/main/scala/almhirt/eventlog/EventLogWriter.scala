package almhirt.eventlog

import akka.actor._
import akka.stream.actor.{ ActorSubscriber, ZeroRequestStrategy, ActorPublisherMessage }

object EventLogWriter {

}

private[almhirt] class EventLogWriterImpl(eventLogSelection: ActorSelection) extends ActorSubscriber with ActorLogging {
  override def requestStrategy = ZeroRequestStrategy
  
  def receive: Receive = Actor.emptyBehavior
}