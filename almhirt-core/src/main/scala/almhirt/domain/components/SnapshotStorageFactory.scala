package almhirt.domain.components

import akka.actor.ActorRef
import almhirt.common._
import almhirt.environment._
import almhirt.core.Almhirt

trait SnapshotStorageFactory {
  def createSnapshotStorage(theAlmhirt: Almhirt): AlmValidation[ActorRef]
}