package almhirt.almhirtsystem

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.util.Duration
import almhirt.AlmhirtSystem

trait AlmhirtsystemComponent {
  implicit def system: AlmhirtSystem
}