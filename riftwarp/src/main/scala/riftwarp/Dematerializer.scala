package riftwarp

import language.higherKinds

import scala.reflect.ClassTag
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import riftwarp._
import riftwarp.components._
import riftwarp.ma.HasFunctionObjects

trait RawDematerializer {
  def channel: RiftChannel
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  /** Path to the root */
  def path: List[String]
  def dematerializeRaw: RiftDimension
}

trait Dematerializer[+TDimension <: RiftDimension] extends RawDematerializer {
  import language.higherKinds

  def dematerialize: TDimension
  override def dematerializeRaw: RiftDimension = dematerialize.asInstanceOf[RiftDimension]

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
  def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalBase64EncodedByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TDimension]]
  def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalByteArrayBlobEncoded(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TDimension]]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): AlmValidation[Dematerializer[TDimension]]
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): AlmValidation[Dematerializer[TDimension]]

  def addUri(ident: String, aValue: _root_.java.net.URI): AlmValidation[Dematerializer[TDimension]]
  def addOptionalUri(ident: String, anOptionalValue: Option[_root_.java.net.URI]): AlmValidation[Dematerializer[TDimension]]

  def addUuid(ident: String, aValue: _root_.java.util.UUID): AlmValidation[Dematerializer[TDimension]]
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]): AlmValidation[Dematerializer[TDimension]]

  def addJson(ident: String, aValue: String): AlmValidation[Dematerializer[TDimension]]
  def addOptionalJson(ident: String, anOptionalValue: Option[String]): AlmValidation[Dematerializer[TDimension]]
  def addXml(ident: String, aValue: scala.xml.Node): AlmValidation[Dematerializer[TDimension]]
  def addOptionalXml(ident: String, anOptionalValue: Option[scala.xml.Node]): AlmValidation[Dematerializer[TDimension]]

  def addBlob(ident: String, aValue: Array[Byte]): AlmValidation[Dematerializer[TDimension]] = addBlob(ident, aValue, PropertyPath(ident :: path))
  def addBlob(ident: String, aValue: Array[Byte], name: String): AlmValidation[Dematerializer[TDimension]] = addBlob(ident, aValue, PropertyPathAndIdentifier(ident :: path, name))
  def addBlob(ident: String, aValue: Array[Byte], identifiers: Map[String, String]): AlmValidation[Dematerializer[TDimension]] = addBlob(ident, aValue, PropertyPathAndIdentifiers(ident :: path, identifiers))
  def addBlob(ident: String, aValue: Array[Byte], blobIdentifier: RiftBlobIdentifier): AlmValidation[Dematerializer[TDimension]]
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]]): AlmValidation[Dematerializer[TDimension]] = addOptionalBlob(ident, anOptionalValue, PropertyPath(ident :: path))
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]], name: String): AlmValidation[Dematerializer[TDimension]] = addOptionalBlob(ident, anOptionalValue, PropertyPathAndIdentifier(ident :: path, name))
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]], identifiers: Map[String, String]): AlmValidation[Dematerializer[TDimension]] = addOptionalBlob(ident, anOptionalValue, PropertyPathAndIdentifiers(ident :: path, identifiers))
  def addOptionalBlob(ident: String, anOptionalValue: Option[Array[Byte]], blobIdentifier: RiftBlobIdentifier): AlmValidation[Dematerializer[TDimension]]

  def addComplexSelective(ident: String, decomposer: RawDecomposer, complex: AnyRef): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexSelective(ident: String, decomposer: RawDecomposer, complex: Option[AnyRef]): AlmValidation[Dematerializer[TDimension]]
  /** Uses exactly the RiftDescriptor to look up a Decomposer */
  def addComplexFixed(ident: String, complex: AnyRef, descriptor: RiftDescriptor): AlmValidation[Dematerializer[TDimension]]
  /** Uses exactly the RiftDescriptor to look up a Decomposer */
  def addOptionalComplexFixed(ident: String, complex: Option[AnyRef], descriptor: RiftDescriptor): AlmValidation[Dematerializer[TDimension]]
  /** Looks up a decomposer by first checking, whether aComplex type implements HasRiftDescriptor, if not use the backupDescriptor, otherwise use the class name to create a RiftDescriptor */
  def addComplex(ident: String, complex: AnyRef, backupDescriptor: Option[RiftDescriptor] = None): AlmValidation[Dematerializer[TDimension]] = {
    val rd = complex match {
      case htd: HasRiftDescriptor => htd.riftDescriptor
      case x => backupDescriptor.getOrElse(RiftDescriptor(complex.getClass()))
    }
    addComplexFixed(ident, complex, rd)
  }
  /** Looks up a decomposer by first checking, whether aComplex type implements HasRiftDescriptor, if not use the backupDescriptor, otherwise use the class name to create a RiftDescriptor */
  def addOptionalComplex(ident: String, complex: Option[AnyRef], backupDescriptor: Option[RiftDescriptor] = None): AlmValidation[Dematerializer[TDimension]]
  /** Looks up a decomposer by first checking, whether aComplex type implements HasRiftDescriptor otherwise use the erased type U to determine look up a decomposer */
  def addComplexTyped[U <: AnyRef](ident: String, complex: U)(implicit cU: ClassTag[U]): AlmValidation[Dematerializer[TDimension]] =
    addComplex(ident, complex, Some(RiftDescriptor(cU.runtimeClass)))
  /** Looks up a decomposer by first checking, whether aComplex type implements HasRiftDescriptor otherwise use the erased type U to determine look up a decomposer */
  def addOptionalComplexTyped[U <: AnyRef](ident: String, complex: Option[U])(implicit cU: ClassTag[U]): AlmValidation[Dematerializer[TDimension]]

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMALoose[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMA[M[_], A <: Any](ident: String, ma: Option[M[A]])(implicit mM: Manifest[M[_]], mA: Manifest[A]): AlmValidation[Dematerializer[TDimension]]

  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalPrimitiveMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addMapSkippingUnknownValues[A, B](ident: String, aMap: Map[A, B])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMapSkippingUnknownValues[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: Manifest[A], mB: Manifest[B]): AlmValidation[Dematerializer[TDimension]]

  def addRiftDescriptor(descriptor: RiftDescriptor): AlmValidation[Dematerializer[TDimension]]

  def fail(prob: Problem): AlmValidation[Dematerializer[TDimension]] = prob.failure

}



