package almhirt

import almhirt.common._

package object commanding {
  implicit class CommandOps[T <: Command](self: T) {
    def track(trackId: String): T = self.addMetadata("track-id", trackId)
    def track(implicit ccud: CanCreateUuid): T = track(ccud.getUuid.toString().filterNot(_ == '-'))
    def canBeTracked: Boolean = self.metadata.contains("track-id")
    def trackingId: Option[String] = self.metadata.get("track-id")
  }
}