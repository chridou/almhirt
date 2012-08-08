package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import com.typesafe.config._

trait StandardAlmAkkaComponent extends AlmAkkaComponent {
  val almAkkaContext: AlmAkkaContext = new AlmAkkaByConfig()
  
  private class AlmAkkaByConfig() extends AlmAkkaContextImpl {
    val conf = ConfigFactory.load
    val actorSystem = ActorSystem(conf.getString("almhirt.systemname"))
    val futureDispatcher = actorSystem.dispatchers.lookup("almhirt.future-dispatcher")
    val messageStreamDispatcherName = Some("almhirt.messagestream-dispatcher")
    val messageHubDispatcherName = Some("almhirt.messagehub-dispatcher")
    val shortDuration = conf.getDouble("almhirt.durations.short") seconds
    val mediumDuration = conf.getDouble("almhirt.durations.medium") seconds
    val longDuration = conf.getDouble("almhirt.durations.long") seconds
  }
}

