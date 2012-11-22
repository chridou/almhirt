package almhirt.riftwarp

import scalaz.syntax.validation._
import almhirt.common._

trait RawDematerializer {
  def descriptor: RiftFullDescriptor
  def dematerializeRaw: AlmValidation[AnyRef]
}

trait Dematerializer[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor] extends RawDematerializer {
  /**
   * Xml, Json, etc
   */
  def dematerialize: AlmValidation[TDimension]
  def dematerializeRaw: AlmValidation[AnyRef] = dematerialize.map(_.asInstanceOf[AnyRef])

  def addString(ident: String, aValue: String): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalString(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TDimension, TChannel]]

  def addBoolean(ident: String, aValue: Boolean): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]): AlmValidation[Dematerializer[TDimension, TChannel]]

  def addByte(ident: String, aValue: Byte): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addInt(ident: String, aValue: Int): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addLong(ident: String, aValue: Long): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addBigInt(ident: String, aValue: BigInt): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]): AlmValidation[Dematerializer[TDimension, TChannel]]
  
  def addFloat(ident: String, aValue: Float): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addDouble(ident: String, aValue: Double): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addBigDecimal(ident: String, aValue: BigDecimal): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]): AlmValidation[Dematerializer[TDimension, TChannel]]
  
  def addByteArray(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addBlob(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TDimension, TChannel]]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): AlmValidation[Dematerializer[TDimension, TChannel]]
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalUuid(ident: String, anOptionalValue: Option[ _root_.java.util.UUID]): AlmValidation[Dematerializer[TDimension, TChannel]]

  def addJson(ident: String, aValue: String): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalJson(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addXml(ident: String, aValue: scala.xml.Node): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]): AlmValidation[Dematerializer[TDimension, TChannel]]

  def addComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, aComplexType: U): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[TDimension, TChannel]]

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[Dematerializer[TDimension, TChannel]]
  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[Dematerializer[TDimension, TChannel]]

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit cbsma: CanDematerializePrimitiveMA[M, A, TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] 
  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit cbsma: CanDematerializePrimitiveMA[M, A, TDimension, TChannel]): AlmValidation[Dematerializer[TDimension, TChannel]] 
  
  def addTypeDescriptor(descriptor: TypeDescriptor): AlmValidation[Dematerializer[TDimension, TChannel]]
   
  def fail(prob: Problem): AlmValidation[Dematerializer[TDimension, TChannel]] = prob.failure
  
}

trait DematerializesToString[TChannel <: RiftChannelDescriptor] extends Dematerializer[DimensionString, TChannel] {
}

trait DematerializesToCord[TChannel <: RiftChannelDescriptor] extends Dematerializer[DimensionCord, TChannel] {
}

trait DematerializesToByteArray[TChannel <: RiftChannelDescriptor] extends Dematerializer[DimensionBinary, TChannel] {
}

trait DematerializesToRawMap[TChannel <: RiftChannelDescriptor] extends Dematerializer[DimensionRawMap, TChannel] {
}
