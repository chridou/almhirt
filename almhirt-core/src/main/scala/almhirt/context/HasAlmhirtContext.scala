package almhirt.context

import akka.actor.ActorRef
import almhirt.common._
import almhirt.akkax.{ CircuitControl, ComponentId }
import almhirt.herder.HerderMessages

trait HasAlmhirtContext {
  implicit def almhirtContext: AlmhirtContext
}