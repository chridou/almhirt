package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.duration._
import com.typesafe.config._

trait AlmAkkaComponentForTesting extends AlmAkkaComponent {
  implicit def almAkkaContext: AlmAkkaContext = TheSoleInstance.instance
  
  private object TheSoleInstance {
    lazy val instance: AlmAkkaContextImpl = new AlmAkkaForTesting()
  }
  
  private class AlmAkkaForTesting() extends AlmAkkaContextImpl {
    val actorSystem = ActorSystem("almhirt")
    val futureDispatcher = actorSystem.dispatcher
    val messageStreamDispatcherName = None
    val messageHubDispatcherName = None
    val shortDuration = 1 seconds
    val mediumDuration = 1 seconds
    val longDuration = 1 seconds
  }
}
