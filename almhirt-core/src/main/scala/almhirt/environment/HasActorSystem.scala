package almhirt.environment

import akka.actor.ActorSystem

trait HasActorSystem {
  def actorSystem: ActorSystem
}