package riftwarp.std

import riftwarp._

object ToWarpPackageDematerializer extends Dematerializer[WarpPackage] {
  override val channel = WarpChannels.`rift-package`

  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): WarpPackage = what

}