package riftwarp.impl.dematerializers

import language.higherKinds

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._
import riftwarp.ma.HasFunctionObjects

trait NoneIsHandledUnified[TDimension <: RiftDimension] { dematerializer: Dematerializer[TDimension] =>
  protected def noneHandler(ident: String): AlmValidation[Dematerializer[TDimension]]
  
  override def addOptionalString(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addString(ident, _), noneHandler(ident))

  override def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]) = option.cata(anOptionalValue)(addBoolean(ident, _), noneHandler(ident))

  override def addOptionalByte(ident: String, anOptionalValue: Option[Byte]) = option.cata(anOptionalValue)(addByte(ident, _), noneHandler(ident))
  override def addOptionalInt(ident: String, anOptionalValue: Option[Int]) = option.cata(anOptionalValue)(addInt(ident, _), noneHandler(ident))
  override def addOptionalLong(ident: String, anOptionalValue: Option[Long]) = option.cata(anOptionalValue)(addLong(ident, _), noneHandler(ident))
  override def  addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]) = option.cata(anOptionalValue)(addBigInt(ident, _), noneHandler(ident))

  override def  addOptionalFloat(ident: String, anOptionalValue: Option[Float]) = option.cata(anOptionalValue)(addFloat(ident, _), noneHandler(ident))
  override def  addOptionalDouble(ident: String, anOptionalValue: Option[Double]) = option.cata(anOptionalValue)(addDouble(ident, _), noneHandler(ident))
  override def  addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]) = option.cata(anOptionalValue)(addBigDecimal(ident, _), noneHandler(ident))

  override def  addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addByteArray(ident, _), noneHandler(ident))
  override def  addOptionalBase64EncodedByteArray(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addBase64EncodedByteArray(ident, _), noneHandler(ident))
  override def  addOptionalByteArrayBlobEncoded(ident: String, anOptionalValue: Option[Array[Byte]]) = option.cata(anOptionalValue)(addByteArrayBlobEncoded(ident, _), noneHandler(ident))

  override def  addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]) = option.cata(anOptionalValue)(addDateTime(ident, _), noneHandler(ident))

  override def  addOptionalUri(ident: String, anOptionalValue: Option[_root_.java.net.URI]) = option.cata(anOptionalValue)(addUri(ident, _), noneHandler(ident))

  override def  addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]) = option.cata(anOptionalValue)(addUuid(ident, _), noneHandler(ident))

  override def  addOptionalJson(ident: String, anOptionalValue: Option[String]) = option.cata(anOptionalValue)(addJson(ident, _), noneHandler(ident))
  override def  addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]) = option.cata(anOptionalValue)(addXml(ident, _), noneHandler(ident))

  override def  addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]], blobIdentifier: RiftBlobIdentifier) = option.cata(anOptionalValue)(addBlob(ident, _, blobIdentifier), noneHandler(ident))

  override def  addOptionalComplexSelective(ident: String, decomposer: RawDecomposer, complex: Option[AnyRef]) = option.cata(complex)(addComplexSelective(ident, decomposer, _), noneHandler(ident))
  override def  addOptionalComplexFixed(ident: String, complex: Option[AnyRef], descriptor: RiftDescriptor) = option.cata(complex)(addComplexFixed(ident, _, descriptor), noneHandler(ident))
  override def  addOptionalComplex(ident: String, complex: Option[AnyRef], backupDescriptor: Option[RiftDescriptor] = None) = option.cata(complex)(addComplex(ident, _, backupDescriptor), noneHandler(ident))
  override def  addOptionalComplexTyped[U <: AnyRef](ident: String, complex: Option[U])(implicit cU: ClassTag[U]): AlmValidation[Dematerializer[TDimension]] = option.cata(complex)(addComplexTyped[U](ident, _), noneHandler(ident))
  
  override def  addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]) = option.cata(ma)(addPrimitiveMA(ident, _), noneHandler(ident))
  override def  addOptionalComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]) = option.cata(ma)(addComplexMA(decomposer)(ident, _), noneHandler(ident))
  override def  addOptionalComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]) = option.cata(ma)(addComplexMAFixed(ident, _), noneHandler(ident))
  override def  addOptionalComplexMALoose[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]) = option.cata(ma)(addComplexMALoose(ident, _), noneHandler(ident))
  override def  addOptionalMA[M[_], A <: Any](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]) = option.cata(ma)(addMA(ident, _), noneHandler(ident))

  override def  addOptionalPrimitiveMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]) = option.cata(aMap)(addPrimitiveMap(ident, _), noneHandler(ident))
  override def  addOptionalComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]) = option.cata(aMap)(addComplexMap(decomposer)(ident, _), noneHandler(ident))
  override def  addOptionalComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]) = option.cata(aMap)(addComplexMapFixed(ident, _), noneHandler(ident))
  override def  addOptionalComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]) = option.cata(aMap)(addComplexMapLoose(ident, _), noneHandler(ident))
  override def  addOptionalMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]) = option.cata(aMap)(addMap(ident, _), noneHandler(ident))
  override def  addOptionalMapSkippingUnknownValues[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]) = option.cata(aMap)(addMapSkippingUnknownValues(ident, _), noneHandler(ident))
}

trait NoneIsOmmitted[TDimension <: RiftDimension] { dematerializer: Dematerializer[TDimension] with NoneIsHandledUnified[TDimension] =>
  protected override def noneHandler(ident: String): AlmValidation[Dematerializer[TDimension]] = dematerializer.success
}
