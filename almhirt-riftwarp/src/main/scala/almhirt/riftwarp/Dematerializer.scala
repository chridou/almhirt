package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDematerializer {
  def tChannel: Class[_ <: RiftChannelDescriptor]
  def tDimension: Class[_ <: RiftTypedDimension[_]]
  def descriptor: RiftFullDescriptor
  def dematerializeRaw: AlmValidation[AnyRef]
}

trait Dematerializer[TChannel <: RiftChannelDescriptor, TDimension <: RiftTypedDimension[_]] extends RawDematerializer {
  /**
   * Xml, Json, etc
   */
  def dematerialize: AlmValidation[TDimension]
  def dematerializeRaw: AlmValidation[AnyRef] = dematerialize.map(_.asInstanceOf[AnyRef])

  def addString(ident: String, aValue: String): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalString(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TChannel, TDimension]]

  def addBoolean(ident: String, aValue: Boolean): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]): AlmValidation[Dematerializer[TChannel, TDimension]]

  def addByte(ident: String, aValue: Byte): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addInt(ident: String, aValue: Int): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addLong(ident: String, aValue: Long): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addBigInt(ident: String, aValue: BigInt): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]): AlmValidation[Dematerializer[TChannel, TDimension]]
  
  def addFloat(ident: String, aValue: Float): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addDouble(ident: String, aValue: Double): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addBigDecimal(ident: String, aValue: BigDecimal): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]): AlmValidation[Dematerializer[TChannel, TDimension]]
  
  def addByteArray(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addBlob(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TChannel, TDimension]]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): AlmValidation[Dematerializer[TChannel, TDimension]]
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalUuid(ident: String, anOptionalValue: Option[ _root_.java.util.UUID]): AlmValidation[Dematerializer[TChannel, TDimension]]

  def addJson(ident: String, aValue: String): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalJson(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addXml(ident: String, aValue: scala.xml.Node): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]): AlmValidation[Dematerializer[TChannel, TDimension]]

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[TChannel, TDimension]]

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[Dematerializer[TChannel, TDimension]]
  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[TChannel, TDimension]]

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A], dematerialzeMA: M[A] => AlmValidation[TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]] 
  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]], dematerialzeMA: M[A] => AlmValidation[TDimension]): AlmValidation[Dematerializer[TChannel, TDimension]] 

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A]): AlmValidation[Dematerializer[TChannel, TDimension]] 
  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]]): AlmValidation[Dematerializer[TChannel, TDimension]] 
  
  def addTypeDescriptor(descriptor: TypeDescriptor): AlmValidation[Dematerializer[TChannel, TDimension]]
   
  def fail(prob: Problem): AlmValidation[Dematerializer[TChannel, TDimension]] = prob.failure
  
}

abstract class BaseDematerializer[TChannel <: RiftChannelDescriptor, TDimension <: RiftTypedDimension[_]](mChannel: Manifest[TChannel], mDimension: Manifest[TDimension]) extends Dematerializer[TChannel, TDimension]{
  val tChannel = mChannel.erasure
  val tDimension = mDimension.erasure
}

abstract class ToStringDematerializer[TChannel <: RiftChannelDescriptor](mChannel: Manifest[TChannel]) extends BaseDematerializer[TChannel, DimensionString](mChannel, manifest[DimensionString]) {
}

abstract class ToCordDematerializer[TChannel <: RiftChannelDescriptor](mChannel: Manifest[TChannel]) extends BaseDematerializer[TChannel, DimensionCord](mChannel, manifest[DimensionCord]) {
}

abstract class ToBinaryDematerializer[TChannel <: RiftChannelDescriptor](mChannel: Manifest[TChannel]) extends BaseDematerializer[TChannel, DimensionBinary](mChannel, manifest[DimensionBinary]) {
}

abstract class ToRawMapDematerializer[TChannel <: RiftChannelDescriptor](mChannel: Manifest[TChannel]) extends BaseDematerializer[TChannel, DimensionRawMap](mChannel, manifest[DimensionRawMap]) {
}
