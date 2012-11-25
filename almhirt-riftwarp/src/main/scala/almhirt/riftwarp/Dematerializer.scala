package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDematerializer {
  def channel: RiftChannel
  def dimension: RiftDimension[_]
  def toolGroup: ToolGroup
  def dematerializeRaw: AlmValidation[AnyRef]
}

trait Dematerializer[TManifestation <: Manifestation[_]] extends RawDematerializer {
  /**
   * Xml, Json, etc
   */
  def dematerialize: AlmValidation[TManifestation]
  def dematerializeRaw: AlmValidation[AnyRef] = dematerialize.map(_.asInstanceOf[AnyRef])

  def addString(ident: String, aValue: String): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalString(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TManifestation]]

  def addBoolean(ident: String, aValue: Boolean): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]): AlmValidation[Dematerializer[TManifestation]]

  def addByte(ident: String, aValue: Byte): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]): AlmValidation[Dematerializer[TManifestation]]
  def addInt(ident: String, aValue: Int): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]): AlmValidation[Dematerializer[TManifestation]]
  def addLong(ident: String, aValue: Long): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]): AlmValidation[Dematerializer[TManifestation]]
  def addBigInt(ident: String, aValue: BigInt): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]): AlmValidation[Dematerializer[TManifestation]]
  
  def addFloat(ident: String, aValue: Float): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]): AlmValidation[Dematerializer[TManifestation]]
  def addDouble(ident: String, aValue: Double): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]): AlmValidation[Dematerializer[TManifestation]]
  def addBigDecimal(ident: String, aValue: BigDecimal): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]): AlmValidation[Dematerializer[TManifestation]]
  
  def addByteArray(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TManifestation]]
  def addBlob(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TManifestation]]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): AlmValidation[Dematerializer[TManifestation]]
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalUuid(ident: String, anOptionalValue: Option[ _root_.java.util.UUID]): AlmValidation[Dematerializer[TManifestation]]

  def addJson(ident: String, aValue: String): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalJson(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TManifestation]]
  def addXml(ident: String, aValue: scala.xml.Node): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]): AlmValidation[Dematerializer[TManifestation]]

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[TManifestation]]

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[Dematerializer[TManifestation]]
  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[TManifestation]]

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TManifestation]] 
  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TManifestation]] 
  
  def addTypeDescriptor(descriptor: TypeDescriptor): AlmValidation[Dematerializer[TManifestation]]
   
  def fail(prob: Problem): AlmValidation[Dematerializer[TManifestation]] = prob.failure
  
}

abstract class BaseDematerializer[TManifestation <: Manifestation[_]](val channel: RiftChannel, val dimension: RiftDimension[_], val toolGroup: ToolGroup) extends Dematerializer[TChannel, TManifestation]

abstract class ToStringDematerializer(channel: RiftChannel, toolGroup: ToolGroup) extends BaseDematerializer[ManifestationString](channel, Dim ,toolGroup) {
}

abstract class ToCordDematerializer(channel: RiftChannel) extends BaseDematerializer[ManifestationCord](mChannel, manifest[DimensionCord]) {
}

abstract class ToBinaryDematerializer(channel: RiftChannel) extends BaseDematerializer[ManifestationBinary](mChannel, manifest[DimensionBinary]) {
}

abstract class ToRawMapDematerializer(channel: RiftChannel) extends BaseDematerializer[ManifestationRawMap](mChannel, manifest[DimensionRawMap]) {
}
