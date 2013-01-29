package almhirt.core

import scala.language.implicitConversions
import akka.actor.ActorSystem

trait HasActorSystem {
  def actorSystem: ActorSystem
}

object HasActorSystem {
  implicit def actorSystem2HasActorSystem(anActorSystem: ActorSystem): HasActorSystem =
    new HasActorSystem{ val actorSystem = anActorSystem}
}