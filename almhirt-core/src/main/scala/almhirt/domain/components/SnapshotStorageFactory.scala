package almhirt.domain.components

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt
import almhirt.environment.configuration.ActorRefComponentFactory

trait SnapshotStorageFactory extends ActorRefComponentFactory {
  def createActorRefComponent(args: Map[String, Any]): AlmValidation[ActorRef]
  def createSnapshotStorage(theAlmhirt: Almhirt): AlmValidation[ActorRef] = createActorRefComponent(Map(("almhirt" -> theAlmhirt)))
}