package riftwarp

import language.higherKinds
import scala.reflect.ClassTag
import scala.collection.IterableLike
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

  def addString(ident: String, aValue: String): Dematerializer[TDimension]
  def addOptionalString(ident: String, anOptionalValue: Option[String]): Dematerializer[TDimension]

  def addBoolean(ident: String, aValue: Boolean): Dematerializer[TDimension]
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]): Dematerializer[TDimension]

  def addByte(ident: String, aValue: Byte): Dematerializer[TDimension]
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]):Dematerializer[TDimension]
  def addInt(ident: String, aValue: Int): Dematerializer[TDimension]
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]): Dematerializer[TDimension]
  def addLong(ident: String, aValue: Long): Dematerializer[TDimension]
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]): Dematerializer[TDimension]
  def addBigInt(ident: String, aValue: BigInt): Dematerializer[TDimension]
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]): Dematerializer[TDimension]

  def addFloat(ident: String, aValue: Float): Dematerializer[TDimension]
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]): Dematerializer[TDimension]
  def addDouble(ident: String, aValue: Double): Dematerializer[TDimension]
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]): Dematerializer[TDimension]
  def addBigDecimal(ident: String, aValue: BigDecimal): Dematerializer[TDimension]
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]): Dematerializer[TDimension]

  def addByteArray(ident: String, aValue: Array[Byte]): Dematerializer[TDimension]
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): Dematerializer[TDimension]
  def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]): Dematerializer[TDimension]
  def addOptionalBase64EncodedByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): Dematerializer[TDimension]
  def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]): Dematerializer[TDimension]
  def addOptionalByteArrayBlobEncoded(ident: String, anOptionalValue: Option[Array[Byte]]): Dematerializer[TDimension]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): Dematerializer[TDimension]
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): Dematerializer[TDimension]

  def addUri(ident: String, aValue: _root_.java.net.URI): Dematerializer[TDimension]
  def addOptionalUri(ident: String, anOptionalValue: Option[_root_.java.net.URI]): Dematerializer[TDimension]

  def addUuid(ident: String, aValue: _root_.java.util.UUID): Dematerializer[TDimension]
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]): Dematerializer[TDimension]

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

  def addPrimitiveMA[M[_], A](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalPrimitiveMA[M[_], A](ident: String, ma: Option[M[A]])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMA[M[_], A <: AnyRef](decomposer: Decomposer[A])(ident: String, ma: Option[M[A]])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMAFixed[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMALoose[M[_], A <: AnyRef](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMALoose[M[_], A <: AnyRef](ident: String, ma: Option[M[A]])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addMA[M[_], A <: Any](ident: String, ma: M[A])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMA[M[_], A <: Any](ident: String, ma: Option[M[A]])(implicit mM: ClassTag[M[_]], mA: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]

  def addIterable[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], decomposer: Decomposer[A]): AlmValidation[Dematerializer[TDimension]]
  
  def addPrimitiveMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalPrimitiveMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMap[A, B <: AnyRef](decomposer: Decomposer[B])(ident: String, aMap: Option[Map[A, B]])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMapFixed[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexMapLoose[A, B <: AnyRef](ident: String, aMap: Option[Map[A, B]])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addMap[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMap[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addMapSkippingUnknownValues[A, B](ident: String, aMap: Map[A, B])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMapSkippingUnknownValues[A, B](ident: String, aMap: Option[Map[A, B]])(implicit mA: ClassTag[A], mB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]

  def addRiftDescriptor(descriptor: RiftDescriptor): Dematerializer[TDimension]

  def includeDirect[T <: AnyRef](what: T, decomposer: Decomposer[T]): AlmValidation[Dematerializer[TDimension]] 
  def include(what: AnyRef, riftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]]
  def include[T <: AnyRef](what: T)(implicit tag: ClassTag[T]): AlmValidation[Dematerializer[TDimension]]
  
  def fail(prob: Problem): AlmValidation[Dematerializer[TDimension]] = prob.failure
  def ok: AlmValidation[Dematerializer[TDimension]] = this.success

}



