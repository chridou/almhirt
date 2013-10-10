package riftwarp.std

import riftwarp.WarpPackage
import riftwarp.Dematerializer

object ToWarpPackageDematerializer extends Dematerializer[WarpPackage] {
  
  override val channel = "warppackage"
  override val dimension = WarpPackage.getClass().getName()

  override def dematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): WarpPackage = what

}