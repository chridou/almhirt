package riftwarp

import language.higherKinds
import scala.reflect.ClassTag
import scala.collection.{IterableLike, MapLike }
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
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]): Dematerializer[TDimension]
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

  def addWith[A](ident: String, what: A, decomposes: Decomposes[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalWith[A](ident: String, what: Option[A], decomposes: Decomposes[A]): AlmValidation[Dematerializer[TDimension]]
  
  /**
   * Dematerialize an complex type with a looked up decomposer
   * The decomposer is looked up in the following order:
   * 1) Check whether the element implements [[riftwarp.components.HasRiftDescriptor]] and use that one
   * 2) Use the elements runtime type
   * 3) Use the backupDescriptor, if present
   */
  def addComplex[A <: AnyRef](ident: String, what: A, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplex[A <: AnyRef](ident: String, what: Option[A], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]]

  def addComplexByTag[A <: AnyRef](ident: String, what: A)(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalComplexByTag[A <: AnyRef](ident: String, what: Option[A])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]

  /**
   * Dematerialize an Iterable of complex types using the given Decomposer
   */
  def addIterableAllWith[A, Coll](ident: String, what: IterableLike[A, Coll], decomposes: Decomposes[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalIterableAllWith[A, Coll](ident: String, what: Option[IterableLike[A, Coll]], decomposes: Decomposes[A]): AlmValidation[Dematerializer[TDimension]]

  /**
   * Dematerialize an Iterable of complex types.
   * The Decomposer will be looked up exactly once and be used for the entire Iterable
   * First the supplied RiftDescriptor will be used for lookup, then the ClassTag
   */
  def addIterableStrict[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalIterableStrict[A <: AnyRef, Coll](ident: String, what: Option[IterableLike[A, Coll]], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]

  /**
   * Dematerialize an Iterable of complex types using a separate decomposer for each element
   * The decomposer is looked up in the following order:
   * 1) Check whether the element implements [[riftwarp.components.HasRiftDescriptor]] and use that one
   * 2) Use the elements runtime type
   * 3) Use the backupDescriptor, if present
   */
  def addIterableOfComplex[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalIterableOfComplex[A <: AnyRef, Coll](ident: String, what: Option[IterableLike[A, Coll]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]]

  /**
   * Dematerialize an Iterable of Riftwarp's primitive types.
   */
  def addIterableOfPrimitives[A, Coll](ident: String, what: IterableLike[A, Coll])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalIterableOfPrimitives[A, Coll](ident: String, what: Option[IterableLike[A, Coll]])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]

  /**
   * Dematerialize an Iterable of subtypes of A. A may be a complex type or a primitive type.
   * All elements will be checked individually on how to decompose them.
   * First the element is checked, whether it is a primitive type. If not, the look up mechanism of 'addIterableOfComplex' is used
   */
  def addIterable[A, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalIterable[A, Coll](ident: String, what: Option[IterableLike[A, Coll]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]]

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

  /**
   * Dematerialize a Map with keys being of primitive type A and values of type B using the given Decomposer for Bs
   */
  def addMapAllWith[A, B](ident: String, what: scala.collection.Map[A,B], decomposes: Decomposes[B])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMapAllWith[A, B](ident: String, what: Option[scala.collection.Map[A,B]], decomposes: Decomposes[B])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]

    /**
   * Dematerialize a Map with keys being of primitive type A and values of type B
   * The Decomposer will be looked up exactly once and be used for all values
   * First the supplied RiftDescriptor will be used for lookup, then the ClassTag
   */
  def addMapStrict[A, B <: AnyRef](ident: String, what: scala.collection.Map[A,B], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMapStrict[A, B <: AnyRef](ident: String, what: Option[scala.collection.Map[A,B]], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]

  /**
   * Dematerialize a Map with keys being of primitive type A and values of type B using a separate decomposer for each value B
   * The decomposer is looked up in the following order:
   * 1) Check whether the element implements [[riftwarp.components.HasRiftDescriptor]] and use that one
   * 2) Use the elements runtime type
   * 3) Use the backupDescriptor, if present
   */
  def addMapOfComplex[A, B <: AnyRef](ident: String, what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMapOfComplex[A, B <: AnyRef](ident: String, what: Option[scala.collection.Map[A,B]], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]

  def addMapOfPrimitives[A, B](ident: String, what: scala.collection.Map[A,B])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMapOfPrimitives[A, B](ident: String, what: Option[scala.collection.Map[A,B]])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[Dematerializer[TDimension]]
    
  /**
   * Dematerialize a Map with keys being of primitive type A and values of type B using a separate decomposer for each value B
   * All elements will be checked individually on how to decompose them.
   * First the element is checked, whether it is a primitive type. If not, the look up mechanism of 'addMapOfComplex' is used
   */
  def addMap[A, B](ident: String, what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  def addOptionalMap[A, B](ident: String, what: Option[scala.collection.Map[A,B]], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[Dematerializer[TDimension]]
  
  
  def addRiftDescriptor(descriptor: RiftDescriptor): Dematerializer[TDimension]

  def includeDirect[T <: AnyRef](what: T, decomposer: Decomposer[T]): AlmValidation[Dematerializer[TDimension]]
  def include(what: AnyRef, riftDescriptor: Option[RiftDescriptor]): AlmValidation[Dematerializer[TDimension]]
  def include[T <: AnyRef](what: T)(implicit tag: ClassTag[T]): AlmValidation[Dematerializer[TDimension]]

  def fail(prob: Problem): AlmValidation[Dematerializer[TDimension]] = prob.failure
  def ok: AlmValidation[Dematerializer[TDimension]] = this.success

}



