package riftwarp.std

import almhirt.common._
import riftwarp._

object FromWarpPackageRematerializer extends Rematerializer[WarpPackage] {
  override val channel = WarpChannels.`rift-package`
  override def rematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    scalaz.Success(what)
}