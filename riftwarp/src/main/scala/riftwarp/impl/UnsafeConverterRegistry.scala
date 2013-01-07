package riftwarp.impl

import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._

class UnsafeDimensionConverterRegistry extends HasDimensionConverters {
  private var converters = Map.empty[String, RawDimensionConverter]
  private var convertersBySource = Map.empty[String, List[RawDimensionConverter]]
  private var convertersByTarget = Map.empty[String, List[RawDimensionConverter]]

  def addConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](converter: DimensionConverter[DimSource, DimTarget]) {
    val ident = converter.tSource.getName()+converter.tTarget.getName()
    converters = converters + (ident -> converter)
    if(!convertersBySource.contains(converter.tSource.getName()))
      convertersBySource = convertersBySource + (converter.tSource.getName() -> (converter :: Nil))
    else
      convertersBySource = convertersBySource + (converter.tSource.getName() -> (converter :: convertersBySource(converter.tSource.getName())))
    if(!convertersByTarget.contains(converter.tTarget.getName()))
      convertersByTarget = convertersByTarget + (converter.tTarget.getName() -> (converter :: Nil))
    else
      convertersByTarget = convertersByTarget + (converter.tTarget.getName() -> (converter :: convertersByTarget(converter.tTarget.getName())))
  }

  def tryGetConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension](implicit mS: Manifest[DimSource], mT: Manifest[DimTarget]): Option[DimensionConverter[DimSource, DimTarget]] =
    converters.get(mS.runtimeClass.getName()+mT.runtimeClass.getName()).map(_.asInstanceOf[DimensionConverter[DimSource, DimTarget]])

  def getConvertersFromDimType(tSource: Class[_ <: RiftDimension]): List[DimensionConverter[_, _]] =
    convertersBySource.get(tSource.getName()).map(_.asInstanceOf[List[DimensionConverter[_, _]]]).getOrElse(Nil)
    
  def getConvertersTo[DimTarget <: RiftDimension](implicit mT: Manifest[DimTarget]): List[DimensionConverter[_, DimTarget]] =
    convertersByTarget.get(mT.runtimeClass.getName()).map(_.asInstanceOf[List[DimensionConverter[_, DimTarget]]]).getOrElse(Nil)
}