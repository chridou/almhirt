package almhirt.environment

import scalaz.std._
import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.commanding._
import almhirt.domain._
import almhirt.util._
import almhirt.messaging._
import org.joda.time.DateTime
import com.typesafe.config.Config
import akka.actor.ActorSystem

trait Almhirt 	extends HasActorSystem
				with CreatesMessageChannels 
				with CanCreateUuidsAndDateTimes 
				with HasDurations 
				with HasExecutionContext
				with HasServices
				with HasConfig {
  override def config: Config
  override def actorSystem: ActorSystem
  def reportProblem(prob: Problem): Unit
  def reportOperationState(opState: OperationState): Unit
  def broadcastDomainEvent(event: DomainEvent): Unit
  def broadcast[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty): Unit
  
  def createMessage[T <: AnyRef](payload: T, metaData: Map[String, String] = Map.empty) = {
    val header = MessageHeader(this.getUuid, None, Map.empty, this.getDateTime)
    Message(header, payload)
  }

  def log: akka.event.LoggingAdapter
  
}