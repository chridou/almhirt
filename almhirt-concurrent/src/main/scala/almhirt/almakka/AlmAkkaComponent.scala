package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.Duration


trait AlmAkkaComponent {
  def almAkkaContext: AlmAkkaContext
  
  trait AlmAkkaContextImpl extends AlmAkkaContext {
    def start() { AlmAkka.theInstance = this }
    def stop(){ actorSystem.shutdown }
  }
}
