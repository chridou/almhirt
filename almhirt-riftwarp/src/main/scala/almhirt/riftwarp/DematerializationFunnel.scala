package almhirt.riftwarp

import almhirt.common._
import scalaz.syntax.validation._

import scalaz.std._
trait NoneHasNoEffectDematerializationFunnel[TDimension <: RiftTypedDimension[_], TChannel <: RiftChannelDescriptor] { dematerializer: Dematerializer[TDimension, TChannel] =>
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

  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]) = option.cata(anOptionalComplexType)(addComplexType(decomposer)(ident, _), this.success)

  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]) = option.cata(anOptionalComplexType)(addComplexType(ident, _), this.success)

  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit cbsma: CanDematerializePrimitiveMA[M, A, TDimension, TChannel]) = option.cata(ma)(addPrimitiveMA(ident, _)(cbsma), this.success) 
  
}