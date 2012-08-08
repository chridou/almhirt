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
    def actorSystem = ActorSystem("almhirt")
    def futureDispatcher = actorSystem.dispatcher
    def messageStreamDispatcherName = None
    def messageHubDispatcherName = None
    def shortDuration = 1 seconds
    def mediumDuration = 1 seconds
    def longDuration = 1 seconds
  }
}
