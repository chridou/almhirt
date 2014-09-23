package almhirt.eventlog

import scala.concurrent.duration.FiniteDuration
import akka.actor._
import akka.stream.actor.{ ActorSubscriber, ZeroRequestStrategy, ActorPublisherMessage }

object EventLogWriter {

}

private[almhirt] class EventLogWriterImpl(eventLogSelection: ActorSelection, lookupInterval: FiniteDuration, maxLookupDuration: FiniteDuration) extends ActorSubscriber with ActorLogging {
  override def requestStrategy = ZeroRequestStrategy
  
  def receiveInitialize: Receive = {
    case _ => ???
  }
 
  def receiveRunning(eventLog: ActorRef): Receive = {
    case _ => ???
  }
  
  
  def receive: Receive = receiveInitialize
}