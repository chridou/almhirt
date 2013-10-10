package riftwarp.std

import almhirt.common._
import riftwarp.Rematerializer
import riftwarp.WarpPackage

object FromWarpPackageRematerializer extends Rematerializer[WarpPackage] {
  import scala.util.parsing.json._
  override def rematerialize(what: WarpPackage, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage] =
    scalaz.Success(what)
}