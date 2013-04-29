package riftwarp

import language.higherKinds
import almhirt.common._

trait Rematerializer[T] extends Function1[T, AlmValidation[WarpPackage]] {
  final def apply(what: T): AlmValidation[WarpPackage] = rematerialize(what)
  def rematerialize(what: T): AlmValidation[WarpPackage]
}