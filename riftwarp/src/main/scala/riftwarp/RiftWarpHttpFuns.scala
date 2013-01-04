package riftwarp

import scalaz.syntax.validation._
import almhirt.common._

object RiftWarpHttpFuns {
  def prepareStringResponse[TStringDimension <: RiftStringBasedDimension](channel: RiftChannel): AlmValidation[(List[String], String)] =
    sys.error("")
}