package almhirt.riftwarp

import almhirt.common._
import scalaz.syntax.validation._

import scalaz.std._
trait NoneHasNoEffectDematerializationFunnel[TDimension <: RiftDimension] { dematerializer: Dematerializer[TDimension] =>
  def addOptionalString(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addString(ident, _), dematerializer.success)

  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]) = option.cata(anOptionalValue)(addBoolean(ident, _), dematerializer.success)

  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]) = option.cata(anOptionalValue)(addByte(ident, _), dematerializer.success)
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]) = option.cata(anOptionalValue)(addInt(ident, _), dematerializer.success)
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]) = option.cata(anOptionalValue)(addLong(ident, _), dematerializer.success)
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]) = option.cata(anOptionalValue)(addBigInt(ident, _), dematerializer.success)
  
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]) = option.cata(anOptionalValue)(addFloat(ident, _), dematerializer.success)
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]) = option.cata(anOptionalValue)(addDouble(ident, _), dematerializer.success)
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = option.cata(anOptionalValue)(addBigDecimal(ident, _), dematerializer.success)
  
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addByteArray(ident, _), dematerializer.success)
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addBlob(ident, _), dematerializer.success)

  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = option.cata(anOptionalValue)(addDateTime(ident, _), dematerializer.success)
  
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = option.cata(anOptionalValue)(addUuid(ident, _), dematerializer.success)

  def addOptionalJson(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addJson(ident, _), dematerializer.success)
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = option.cata(anOptionalValue)(addXml(ident, _), dematerializer.success)

  def addOptionalComplexType[U <: AnyRef](decomposer: Decomposer[U])(ident: String, anOptionalComplexType: Option[U]) = option.cata(anOptionalComplexType)(addComplexType(decomposer)(ident, _), this.success)

  def addOptionalComplexType[U <: AnyRef](ident: String, anOptionalComplexType: Option[U]) = option.cata(anOptionalComplexType)(addComplexType(ident, _), dematerializer.success)

  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]) = option.cata(ma)(addPrimitiveMA(ident, _), dematerializer.success) 
  
}