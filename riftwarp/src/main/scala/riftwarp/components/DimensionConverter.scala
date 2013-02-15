package riftwarp.components

import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._

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
      case exn: Exception => UnspecifiedProblem("Types do not match. I convert from '%s' to '%s', but you gave me a '%s'".format(tSource, tTarget, source.getClass.getName)).failure
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

object DimensionConverterStringToXmlElem extends DimensionConverter[DimensionString, DimensionXmlElem] {
  val tSource = classOf[DimensionString]
  val tTarget = classOf[DimensionXmlElem]
  def convert(source: DimensionString): AlmValidation[DimensionXmlElem] =
    almhirt.xml.funs.xmlFromString(source.manifestation).map(DimensionXmlElem(_))
}

object DimensionConverterCordToXmlElem extends DimensionConverter[DimensionCord, DimensionXmlElem] {
  val tSource = classOf[DimensionCord]
  val tTarget = classOf[DimensionXmlElem]
  def convert(source: DimensionCord): AlmValidation[DimensionXmlElem] =
    almhirt.xml.funs.xmlFromString(source.manifestation.toString).map(DimensionXmlElem(_))
}

object DimensionConverterXmlElemToString extends DimensionConverter[DimensionXmlElem, DimensionString] {
  val tSource = classOf[DimensionXmlElem]
  val tTarget = classOf[DimensionString]
  def convert(source: DimensionXmlElem): AlmValidation[DimensionString] =
    DimensionString(source.manifestation.toString).success
}

object DimensionConverterXmlElemToCord extends DimensionConverter[DimensionXmlElem, DimensionCord] {
  val tSource = classOf[DimensionXmlElem]
  val tTarget = classOf[DimensionCord]
  def convert(source: DimensionXmlElem): AlmValidation[DimensionCord] =
    DimensionCord(source.manifestation.toString).success
}

object DimensionConverterXmlElemToNiceString extends DimensionConverter[DimensionXmlElem, DimensionNiceString] {
  val tSource = classOf[DimensionXmlElem]
  val tTarget = classOf[DimensionNiceString]
  def convert(source: DimensionXmlElem): AlmValidation[DimensionNiceString] =
    DimensionNiceString(source.manifestation.buildString(false)).success
}

object DimensionConverterXmlElemToNiceCord extends DimensionConverter[DimensionXmlElem, DimensionNiceCord] {
  val tSource = classOf[DimensionXmlElem]
  val tTarget = classOf[DimensionNiceCord]
  def convert(source: DimensionXmlElem): AlmValidation[DimensionNiceCord] =
    DimensionNiceCord(source.manifestation.buildString(false)).success
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

