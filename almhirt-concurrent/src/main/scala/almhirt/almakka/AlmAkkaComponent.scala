package almhirt.almakka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.Duration


trait AlmAkkaComponent {
  implicit def almAkkaContext: AlmAkkaContext
  
  def startAlmAkka() = almAkkaContext.asInstanceOf[AlmAkkaContextImpl].start
  def stopAlmAkka() = almAkkaContext.asInstanceOf[AlmAkkaContextImpl].stop
  
  trait AlmAkkaContextImpl extends AlmAkkaContext {
    def start() { }
    def stop(){ actorSystem.shutdown }
  }
}
