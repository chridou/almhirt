package riftwarp.components

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

trait HasDimensionConverters {
  def addConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](converter: DimensionConverter[DimSource, DimTarget])
  def tryGetConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](implicit mS: ClassTag[DimSource], mT: ClassTag[DimTarget]): Option[DimensionConverter[DimSource, DimTarget]]
  def getConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](implicit mS: ClassTag[DimSource], mT: ClassTag[DimTarget]): AlmValidation[DimensionConverter[DimSource, DimTarget]] =
    option.cata(tryGetConverter[DimSource, DimTarget])(
      converter => converter.success,
      UnspecifiedProblem("No converter found for '%s' -> '%s')".format(mS.runtimeClass.getName, mT.runtimeClass.getName)).failure)

  def getConvertersFromByDimType(tSource: Class[_ <: RiftDimension]): List[DimensionConverter[_, _]]
  def getConvertersFrom[DimSource <: RiftDimension](implicit mS: ClassTag[DimSource]): List[DimensionConverter[DimSource, _]] =
    getConvertersFromByDimType(mS.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]]).map(_.asInstanceOf[DimensionConverter[DimSource, _]])
  def getConvertersToByDimType(tTarget: Class[_ <: RiftDimension]): List[DimensionConverter[_, _]]
  def getConvertersTo[DimTarget <: RiftDimension](implicit mT: ClassTag[DimTarget]): List[DimensionConverter[_, DimTarget]] =
    getConvertersToByDimType(mT.runtimeClass.asInstanceOf[Class[_ <: RiftDimension]]).map(_.asInstanceOf[DimensionConverter[_, DimTarget]])
    
}