package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import com.typesafe.config._

trait StandardAlmAkkaComponent extends AlmAkkaComponent {
  implicit def almAkkaContext: AlmAkkaContext = TheSoleInstance.instance
  
  private object TheSoleInstance {
    lazy val instance: AlmAkkaContextImpl = new AlmAkkaByConfig()
  }
  
  private class AlmAkkaByConfig() extends AlmAkkaContextImpl {
    val conf = ConfigFactory.load
    def actorSystem = ActorSystem("almhirt")
    def futureDispatcher = actorSystem.dispatchers.lookup("almhirt.future-dispatcher")
    def messageStreamDispatcherName = Some("messagestream-dispatcher")
    def messageHubDispatcherName = Some("messagehub-dispatcher")
    def shortDuration = conf.getDouble("almhirt.durations.short") seconds
    def mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
    def longDuration = conf.getDouble("almhirt.durations.long") seconds
  }
}