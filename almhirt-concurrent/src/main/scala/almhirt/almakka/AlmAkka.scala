package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import akka.util.Timeout

trait AlmAkkaDefaults {
  implicit def defaultActorSystem = AlmAkka.actorSystem
  implicit def defaultFutureDispatch = AlmAkka.futureDispatcher
  implicit def defaultDuration = AlmAkka.mediumDuration
  implicit def defaultTimeout = Timeout(defaultDuration)
}

object AlmAkka extends AlmAkkaContext {
  private[almakka] var theInstance: AlmAkkaContext = null
  
  def actorSystem = theInstance.actorSystem
  def futureDispatcher = theInstance.futureDispatcher
  def messageStreamDispatcherName = theInstance.messageStreamDispatcherName
  def messageHubDispatcherName = theInstance.messageHubDispatcherName
  def shortDuration = theInstance.shortDuration
  def mediumDuration = theInstance.mediumDuration
  def longDuration = theInstance.longDuration
  
}
