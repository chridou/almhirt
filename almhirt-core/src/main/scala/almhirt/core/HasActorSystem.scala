package almhirt.core

import akka.actor.ActorSystem

trait HasActorSystem {
  def actorSystem: ActorSystem
}