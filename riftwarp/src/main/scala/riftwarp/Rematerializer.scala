package riftwarp

import language.higherKinds
import almhirt.common._

trait Rematerializer[T] extends Function1[T, AlmValidation[WarpPackage]] {
  def channel: WarpChannel
  final def apply(what: T): AlmValidation[WarpPackage] = rematerialize(what)
  def rematerialize(what: T, options: Map[String, Any] = Map.empty): AlmValidation[WarpPackage]
}