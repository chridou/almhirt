package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDematerializer {
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def dematerializeRaw: AlmValidation[AnyRef]
}

trait Dematerializer[TDimension <: RiftDimension] extends RawDematerializer {
  /**
   * Xml, Json, etc
   */
  def dematerialize: AlmValidation[TDimension]
  def dematerializeRaw: AlmValidation[AnyRef] = dematerialize.map(_.asInstanceOf[AnyRef])

  def addString(ident: String, aValue: String): AlmValidation[Dematerializer[TDimension]]
  def addOptionalString(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TDimension]]

  def addBoolean(ident: String, aValue: Boolean): AlmValidation[Dematerializer[TDimension]]
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]): AlmValidation[Dematerializer[TDimension]]

  def addByte(ident: String, aValue: Byte): AlmValidation[Dematerializer[TDimension]]
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]): AlmValidation[Dematerializer[TDimension]]
  def addInt(ident: String, aValue: Int): AlmValidation[Dematerializer[TDimension]]
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]): AlmValidation[Dematerializer[TDimension]]
  def addLong(ident: String, aValue: Long): AlmValidation[Dematerializer[TDimension]]
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]): AlmValidation[Dematerializer[TDimension]]
  def addBigInt(ident: String, aValue: BigInt): AlmValidation[Dematerializer[TDimension]]
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]): AlmValidation[Dematerializer[TDimension]]
  
  def addFloat(ident: String, aValue: Float): AlmValidation[Dematerializer[TDimension]]
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]): AlmValidation[Dematerializer[TDimension]]
  def addDouble(ident: String, aValue: Double): AlmValidation[Dematerializer[TDimension]]
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]): AlmValidation[Dematerializer[TDimension]]
  def addBigDecimal(ident: String, aValue: BigDecimal): AlmValidation[Dematerializer[TDimension]]
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]): AlmValidation[Dematerializer[TDimension]]
  
  def addByteArray(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TDimension]]
  def addBlob(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TDimension]]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): AlmValidation[Dematerializer[TDimension]]
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): AlmValidation[Dematerializer[TDimension]]
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID): AlmValidation[Dematerializer[TDimension]]
  def addOptionalUuid(ident: String, anOptionalValue: Option[ _root_.java.util.UUID]): AlmValidation[Dematerializer[TDimension]]

  def addJson(ident: String, aValue: String): AlmValidation[Dematerializer[TDimension]]
  def addOptionalJson(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TDimension]]
  def addXml(ident: String, aValue: scala.xml.Node): AlmValidation[Dematerializer[TDimension]]
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]): AlmValidation[Dematerializer[TDimension]]

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[TDimension]]

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[TDimension]]

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]] 
  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]] 
  
  def addTypeDescriptor(descriptor: TypeDescriptor): AlmValidation[Dematerializer[TDimension]]
   
  def fail(prob: Problem): AlmValidation[Dematerializer[TDimension]] = prob.failure
  
}

abstract class BaseDematerializer[TDimension <: RiftDimension](val tDimension: Class[_ <: RiftDimension]) extends Dematerializer[TDimension]

abstract class ToStringDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup) extends BaseDematerializer[DimensionString](classOf[DimensionCord])

abstract class ToCordDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup) extends BaseDematerializer[DimensionCord](classOf[DimensionCord])

abstract class ToBinaryDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup) extends BaseDematerializer[DimensionBinary](classOf[DimensionCord])

abstract class ToRawMapDematerializer(val channel: RiftChannel, val toolGroup: ToolGroup) extends BaseDematerializer[DimensionRawMap](classOf[DimensionCord])
