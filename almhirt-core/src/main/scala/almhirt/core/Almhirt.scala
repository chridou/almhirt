package almhirt.core

import scalaz.std._
import akka.actor.ActorSystem
import almhirt.core._
import almhirt.common._
import almhirt.environment._
import almhirt.commanding._
import almhirt.domain._
import almhirt.util._
import almhirt.messaging._
import com.typesafe.config.Config

trait Almhirt 	extends HasActorSystem
				with HasMessageHub
				with CanCreateUuidsAndDateTimes 
				with HasDurations 
				with HasExecutionContext
				with HasServices
				with CanPublishMessages
				with CanPublishItems {
  def log: akka.event.LoggingAdapter
}

object Almhirt {
  def apply(): Almhirt with Disposable = {
    val actorSystem = ActorSystem("Almhirt")
    //val msgHub = MessageHub()
    
    ???
  }
  
  
  implicit class AlmhirtOps(theAlmhirt: Almhirt) {
  }
}