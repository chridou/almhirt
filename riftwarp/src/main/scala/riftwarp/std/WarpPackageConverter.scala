package riftwarp.std

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait WarpPackageConverter[To <: WarpPackage] {
  def convert(what: WarpPackage): AlmValidation[To]
}

trait ToWarpPackageCollectionConverter extends WarpPackageConverter[WarpCollection] {
  def convert(what: WarpPackage): AlmValidation[WarpCollection] =
    what match {
      case x: WarpCollection => x.success
      case x: WarpPrimitive => WarpCollection(Vector(x)).success
      case x: WarpObject => WarpCollection(Vector(x)).success
      case WarpTuple2(a, b) => WarpCollection(Vector(a, b)).success
      case WarpTuple3(a, b, c) => WarpCollection(Vector(a, b, c)).success
      case x => MappingProblem(s""""${x.getClass().getSimpleName()}" cannot be converted to a "WarpCollection".""").failure
    }
}

trait ToWarpPackageAssocCollectionConverter extends WarpPackageConverter[WarpAssociativeCollection] {
  def convert(what: WarpPackage): AlmValidation[WarpAssociativeCollection] =
    what match {
      case x: WarpAssociativeCollection => x.success
      case x => MappingProblem(s""""${x.getClass().getSimpleName()}" cannot be converted to a "WarpAssociativeCollection".""").failure
    }
}
