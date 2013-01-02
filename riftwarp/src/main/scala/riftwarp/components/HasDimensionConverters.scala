package riftwarp.components

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasDimensionConverters {
  def addConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](converter: DimensionConverter[DimSource, DimTarget])
  def tryGetConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](implicit mS: Manifest[DimSource], mT: Manifest[DimTarget]): Option[DimensionConverter[DimSource, DimTarget]]
  def getConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](implicit mS: Manifest[DimSource], mT: Manifest[DimTarget]): AlmValidation[DimensionConverter[DimSource, DimTarget]] =
    option.cata(tryGetConverter[DimSource, DimTarget])(
      converter => converter.success,
      UnspecifiedProblem("No converter found for '%s' -> '%s')".format(mS.runtimeClass.getName, mT.runtimeClass.getName)).failure)

  def getConvertersFrom[DimSource <: RiftDimension](implicit mS: Manifest[DimSource]): List[DimensionConverter[DimSource, _]]
  def getConvertersTo[DimTarget <: RiftDimension](implicit mT: Manifest[DimTarget]): List[DimensionConverter[_, DimTarget]]
}