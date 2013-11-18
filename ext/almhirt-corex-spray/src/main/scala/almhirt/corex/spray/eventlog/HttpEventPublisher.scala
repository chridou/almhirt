package almhirt.corex.spray.eventlog

import scala.concurrent._
import scala.concurrent.duration._
import scalaz._, Scalaz._
import akka.actor._
import almhirt.messaging.MessagePublisher
import almhirt.common._
import almhirt.configuration._
import almhirt.almfuture.all._
import almhirt.serialization._
import almhirt.core.Almhirt
import almhirt.eventlog.EventLog
import spray.http._
import spray.client.pipelining._
import almhirt.corex.spray.SingleTypeHttpPublisher

abstract class HttpEventPublisher()(implicit myAlmhirt: Almhirt) extends SingleTypeHttpPublisher[Event]() {
  import almhirt.eventlog.EventLog._
  
  override def createUri(event: Event): Uri
  def onProblem(event: Event, problem: Problem, respondTo: ActorRef)
  def onSuccess(event: Event, time: FiniteDuration, respondTo: ActorRef)

  def publishEventOverWire(event: Event) {
    publishOverWire(event).onComplete(
      fail => onProblem(event, fail, sender),
      success => onSuccess(success._1, success._2, sender))(myAlmhirt.futuresExecutor)
  }

  override def receive: Receive = {
    case LogEvent(event) => publishEventOverWire(event)
  }

}

