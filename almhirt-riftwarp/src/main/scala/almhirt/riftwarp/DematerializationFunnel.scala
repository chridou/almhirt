package almhirt.riftwarp

import almhirt.common._
import scalaz.syntax.validation._

trait DematerializationFunnel {
  def addString(ident: String, aValue: String): AlmValidation[DematerializationFunnel]
  def addOptionalString(ident: String, anOptionalValue: Option[String]): AlmValidation[DematerializationFunnel]

  def addBoolean(ident: String, aValue: Boolean): AlmValidation[DematerializationFunnel]
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]): AlmValidation[DematerializationFunnel]

  def addByte(ident: String, aValue: Byte): AlmValidation[DematerializationFunnel]
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]): AlmValidation[DematerializationFunnel]
  def addInt(ident: String, aValue: Int): AlmValidation[DematerializationFunnel]
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]): AlmValidation[DematerializationFunnel]
  def addLong(ident: String, aValue: Long): AlmValidation[DematerializationFunnel]
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]): AlmValidation[DematerializationFunnel]
  def addBigInt(ident: String, aValue: BigInt): AlmValidation[DematerializationFunnel]
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]): AlmValidation[DematerializationFunnel]
  
  def addFloat(ident: String, aValue: Float): AlmValidation[DematerializationFunnel]
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]): AlmValidation[DematerializationFunnel]
  def addDouble(ident: String, aValue: Double): AlmValidation[DematerializationFunnel]
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]): AlmValidation[DematerializationFunnel]
  def addBigDecimal(ident: String, aValue: BigDecimal): AlmValidation[DematerializationFunnel]
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]): AlmValidation[DematerializationFunnel]
  
  def addByteArray(ident: String, aValue: Array[Byte]): AlmValidation[DematerializationFunnel]
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[DematerializationFunnel]
  def addBlob(ident: String, aValue: Array[Byte]): AlmValidation[DematerializationFunnel]
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[DematerializationFunnel]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): AlmValidation[DematerializationFunnel]
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): AlmValidation[DematerializationFunnel]
  
  def addUuid(ident: String, aValue: _root_.java.util.UUID): AlmValidation[DematerializationFunnel]
  def addOptionalUuid(ident: String, anOptionalValue: Option[ _root_.java.util.UUID]): AlmValidation[DematerializationFunnel]

  def addJson(ident: String, aValue: String): AlmValidation[DematerializationFunnel]
  def addOptionalJson(ident: String, anOptionalValue: Option[String]): AlmValidation[DematerializationFunnel]
  def addXml(ident: String, aValue: scala.xml.Node): AlmValidation[DematerializationFunnel]
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]): AlmValidation[DematerializationFunnel]

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U, decomposer: Decomposer[U]): AlmValidation[DematerializationFunnel]
  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U], decomposer: Decomposer[U]): AlmValidation[DematerializationFunnel]

  def addComplexType[U <: AnyRef](ident: String, aComplexType: U): AlmValidation[DematerializationFunnel]
  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]): AlmValidation[DematerializationFunnel]

  def addTypeDescriptor(descriptor: TypeDescriptor): AlmValidation[DematerializationFunnel]
   
  def fail(prob: Problem): AlmValidation[DematerializationFunnel] = prob.failure

}

import scalaz.std._
trait NoneHasNoEffectDematerializationFunnel { funnel: DematerializationFunnel =>
  def addOptionalString(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addString(ident, _), this.success)

  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]) = option.cata(anOptionalValue)(addBoolean(ident, _), this.success)

  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]) = option.cata(anOptionalValue)(addByte(ident, _), this.success)
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]) = option.cata(anOptionalValue)(addInt(ident, _), this.success)
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]) = option.cata(anOptionalValue)(addLong(ident, _), this.success)
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]) = option.cata(anOptionalValue)(addBigInt(ident, _), this.success)
  
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]) = option.cata(anOptionalValue)(addFloat(ident, _), this.success)
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]) = option.cata(anOptionalValue)(addDouble(ident, _), this.success)
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = option.cata(anOptionalValue)(addBigDecimal(ident, _), this.success)
  
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addByteArray(ident, _), this.success)
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addBlob(ident, _), this.success)

  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = option.cata(anOptionalValue)(addDateTime(ident, _), this.success)
  
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = option.cata(anOptionalValue)(addUuid(ident, _), this.success)

  def addOptionalJson(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addJson(ident, _), this.success)
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = option.cata(anOptionalValue)(addXml(ident, _), this.success)

  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U], decomposer: Decomposer[U]) = option.cata(anOptionalComplexType)(addComplexType(ident, _, decomposer), this.success)

  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]) = option.cata(anOptionalComplexType)(addComplexType(ident, _), this.success)

}