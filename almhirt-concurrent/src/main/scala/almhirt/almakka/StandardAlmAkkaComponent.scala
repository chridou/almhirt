package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import com.typesafe.config._

trait StandardAlmAkkaComponent extends AlmAkkaComponent {
  def almAkkaContext = TheSoleInstance.instance
  
  private object TheSoleInstance {
    lazy val instance: AlmAkkaContext = new AlmAkkaByConfig()
  }
  
  private final class AlmAkkaByConfig() extends AlmAkkaContext {
    val conf = ConfigFactory.load
    def actorSystem = ActorSystem("almhirt")
    def futureDispatcher = actorSystem.dispatchers.lookup("almhirt.future-dispatcher")
    def messageStreamDispatcherName = "messagestream-dispatcher"
    def messageHubDispatcherName = "messagehub-dispatcher"
    def shortDuration = conf.getDouble("almhirt.durations.short") seconds
    def mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
    def longDuration = conf.getDouble("almhirt.durations.long") seconds
  }
}