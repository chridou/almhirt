package riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDimensionConverter{
  def tSource: Class[_ <: RiftDimension]
  def tTarget: Class[_ <: RiftDimension]
  def convertRaw(source: RiftDimension): AlmValidation[RiftDimension]
  
}

trait DimensionConverter[DimSource <: RiftDimension, DimTarget <: RiftDimension] extends RawDimensionConverter {
  def convert(source: DimSource): AlmValidation[DimTarget]
  def convertRaw(source: RiftDimension) =
    try {
      convert(source.asInstanceOf[DimSource])
    } catch {
      case exn => UnspecifiedProblem("Types do not match").failure
    }
}