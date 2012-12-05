package riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDimensionConverter {
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

object DimensionNiceStringToString extends DimensionConverter[DimensionNiceString, DimensionString] {
  import scalaz.Cord
  val tSource = classOf[DimensionNiceString]
  val tTarget = classOf[DimensionString]
  def convert(source: DimensionNiceString): AlmValidation[DimensionString] =
    DimensionString(source.manifestation).success
}

object DimensionNiceCordToCord extends DimensionConverter[DimensionNiceCord, DimensionCord] {
  import scalaz.Cord
  val tSource = classOf[DimensionNiceCord]
  val tTarget = classOf[DimensionCord]
  def convert(source: DimensionNiceCord): AlmValidation[DimensionCord] =
    DimensionCord(source.manifestation).success
}

object DimensionConverterStringToCord extends DimensionConverter[DimensionString, DimensionCord] {
  import scalaz.Cord
  val tSource = classOf[DimensionString]
  val tTarget = classOf[DimensionCord]
  def convert(source: DimensionString): AlmValidation[DimensionCord] =
    DimensionCord(Cord(source.manifestation)).success
}

object DimensionConverterCordToString extends DimensionConverter[DimensionCord, DimensionString] {
  val tSource = classOf[DimensionCord]
  val tTarget = classOf[DimensionString]
  def convert(source: DimensionCord): AlmValidation[DimensionString] =
    DimensionString(source.manifestation.toString).success
}

// These have to be added manually

object DimensionStringToNiceString extends DimensionConverter[DimensionString, DimensionNiceString] {
  import scalaz.Cord
  val tSource = classOf[DimensionString]
  val tTarget = classOf[DimensionNiceString]
  def convert(source: DimensionString): AlmValidation[DimensionNiceString] =
    DimensionNiceString(source.manifestation).success
}

object DimensionCordToNiceCord extends DimensionConverter[DimensionCord, DimensionNiceCord] {
  import scalaz.Cord
  val tSource = classOf[DimensionCord]
  val tTarget = classOf[DimensionNiceCord]
  def convert(source: DimensionCord): AlmValidation[DimensionNiceCord] =
    DimensionNiceCord(source.manifestation).success
}

