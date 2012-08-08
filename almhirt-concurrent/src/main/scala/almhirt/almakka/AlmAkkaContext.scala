package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.Duration

trait AlmAkkaContext {
  def actorSystem: ActorSystem
  def futureDispatcher: MessageDispatcher
  def messageStreamDispatcherName: Option[String]
  def messageHubDispatcherName: Option[String]
  def shortDuration: Duration
  def mediumDuration: Duration
  def longDuration: Duration
}

