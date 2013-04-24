package riftwarp

import language.higherKinds
import scala.reflect.ClassTag
import scala.collection.{IterableLike, MapLike }
import scalaz.std._
import scalaz.syntax.validation._
import almhirt.common._
import almhirt.serialization._
import riftwarp._
import riftwarp.components._
import riftwarp.warpsequence.WarpValue

trait RawWarpSequencer {
  def channel: RiftChannel
  /** Path to the root */
  def path: List[String]
  def isRoot: Boolean
  def tDimension: Class[_ <: RiftDimension]
  def toolGroup: ToolGroup
  def dematerializeRaw: RiftDimension
}

trait WarpSequencer[TDimension <: RiftDimension] extends RawWarpSequencer {
  import language.higherKinds

  def dematerialize: TDimension
  override def dematerializeRaw: RiftDimension = dematerialize.asInstanceOf[RiftDimension]

  //def writeAll(values: Seq[WarpValue]): WarpSequencer[TDimension]
  
  def addString(ident: String, aValue: String): WarpSequencer[TDimension]
  def addOptionalString(ident: String, anOptionalValue: Option[String]): WarpSequencer[TDimension]

  def addBoolean(ident: String, aValue: Boolean): WarpSequencer[TDimension]
  def addOptionalBoolean(ident: String, anOptionalValue: Option[Boolean]): WarpSequencer[TDimension]

  def addByte(ident: String, aValue: Byte): WarpSequencer[TDimension]
  def addOptionalByte(ident: String, anOptionalValue: Option[Byte]): WarpSequencer[TDimension]
  def addInt(ident: String, aValue: Int): WarpSequencer[TDimension]
  def addOptionalInt(ident: String, anOptionalValue: Option[Int]): WarpSequencer[TDimension]
  def addLong(ident: String, aValue: Long): WarpSequencer[TDimension]
  def addOptionalLong(ident: String, anOptionalValue: Option[Long]): WarpSequencer[TDimension]
  def addBigInt(ident: String, aValue: BigInt): WarpSequencer[TDimension]
  def addOptionalBigInt(ident: String, anOptionalValue: Option[BigInt]): WarpSequencer[TDimension]

  def addFloat(ident: String, aValue: Float): WarpSequencer[TDimension]
  def addOptionalFloat(ident: String, anOptionalValue: Option[Float]): WarpSequencer[TDimension]
  def addDouble(ident: String, aValue: Double): WarpSequencer[TDimension]
  def addOptionalDouble(ident: String, anOptionalValue: Option[Double]): WarpSequencer[TDimension]
  def addBigDecimal(ident: String, aValue: BigDecimal): WarpSequencer[TDimension]
  def addOptionalBigDecimal(ident: String, anOptionalValue: Option[BigDecimal]): WarpSequencer[TDimension]

  def addByteArray(ident: String, aValue: Array[Byte]): WarpSequencer[TDimension]
  def addOptionalByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): WarpSequencer[TDimension]
  def addBase64EncodedByteArray(ident: String, aValue: Array[Byte]): WarpSequencer[TDimension]
  def addOptionalBase64EncodedByteArray(ident: String, anOptionalValue: Option[Array[Byte]]): WarpSequencer[TDimension]
  def addByteArrayBlobEncoded(ident: String, aValue: Array[Byte]): WarpSequencer[TDimension]
  def addOptionalByteArrayBlobEncoded(ident: String, anOptionalValue: Option[Array[Byte]]): WarpSequencer[TDimension]

  def addDateTime(ident: String, aValue: org.joda.time.DateTime): WarpSequencer[TDimension]
  def addOptionalDateTime(ident: String, anOptionalValue: Option[org.joda.time.DateTime]): WarpSequencer[TDimension]

  def addUri(ident: String, aValue: _root_.java.net.URI): WarpSequencer[TDimension]
  def addOptionalUri(ident: String, anOptionalValue: Option[_root_.java.net.URI]): WarpSequencer[TDimension]

  def addUuid(ident: String, aValue: _root_.java.util.UUID): WarpSequencer[TDimension]
  def addOptionalUuid(ident: String, anOptionalValue: Option[_root_.java.util.UUID]): WarpSequencer[TDimension]

  def addWith[A](ident: String, what: A, decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalWith[A](ident: String, what: Option[A], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]]
  
  /**
   * Dematerialize an complex type with a looked up decomposer
   * The decomposer is looked up in the following order:
   * 1) Check whether the element implements [[riftwarp.components.HasRiftDescriptor]] and use that one
   * 2) Use the elements runtime type
   * 3) Use the backupDescriptor, if present
   */
  def addComplex[A <: AnyRef](ident: String, what: A, backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalComplex[A <: AnyRef](ident: String, what: Option[A], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]

  def addComplexByTag[A <: AnyRef](ident: String, what: A)(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalComplexByTag[A <: AnyRef](ident: String, what: Option[A])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]

  /**
   * Dematerialize an Iterable of complex types using the given Decomposer
   */
  def addIterableAllWith[A, Coll](ident: String, what: IterableLike[A, Coll], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalIterableAllWith[A, Coll](ident: String, what: Option[IterableLike[A, Coll]], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]]

  /**
   * Dematerialize an Iterable of complex types.
   * The Decomposer will be looked up exactly once and be used for the entire Iterable
   * First the supplied RiftDescriptor will be used for lookup, then the ClassTag
   */
  def addIterableStrict[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalIterableStrict[A <: AnyRef, Coll](ident: String, what: Option[IterableLike[A, Coll]], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]

  /**
   * Dematerialize an Iterable of complex types using a separate decomposer for each element
   * The decomposer is looked up in the following order:
   * 1) Check whether the element implements [[riftwarp.components.HasRiftDescriptor]] and use that one
   * 2) Use the elements runtime type
   * 3) Use the backupDescriptor, if present
   */
  def addIterableOfComplex[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalIterableOfComplex[A <: AnyRef, Coll](ident: String, what: Option[IterableLike[A, Coll]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]

  /**
   * Dematerialize an Iterable of Riftwarp's primitive types.
   */
  def addIterableOfPrimitives[A, Coll](ident: String, what: IterableLike[A, Coll])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalIterableOfPrimitives[A, Coll](ident: String, what: Option[IterableLike[A, Coll]])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]

  /**
   * Dematerialize an Iterable of subtypes of A. A may be a complex type or a primitive type.
   * All elements will be checked individually on how to decompose them.
   * First the element is checked, whether it is a primitive type. If not, the look up mechanism of 'addIterableOfComplex' is used
   */
  def addIterable[A, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalIterable[A, Coll](ident: String, what: Option[IterableLike[A, Coll]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]

  /**
   * Dematerialize a Map with keys being of primitive type A and values of type B using the given Decomposer for Bs
   */
  def addMapAllWith[A, B](ident: String, what: scala.collection.Map[A,B], decomposes: Decomposes[B])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalMapAllWith[A, B](ident: String, what: Option[scala.collection.Map[A,B]], decomposes: Decomposes[B])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]

    /**
   * Dematerialize a Map with keys being of primitive type A and values of type B
   * The Decomposer will be looked up exactly once and be used for all values
   * First the supplied RiftDescriptor will be used for lookup, then the ClassTag
   */
  def addMapStrict[A, B <: AnyRef](ident: String, what: scala.collection.Map[A,B], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalMapStrict[A, B <: AnyRef](ident: String, what: Option[scala.collection.Map[A,B]], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[WarpSequencer[TDimension]]

  /**
   * Dematerialize a Map with keys being of primitive type A and values of type B using a separate decomposer for each value B
   * The decomposer is looked up in the following order:
   * 1) Check whether the element implements [[riftwarp.components.HasRiftDescriptor]] and use that one
   * 2) Use the elements runtime type
   * 3) Use the backupDescriptor, if present
   */
  def addMapOfComplex[A, B <: AnyRef](ident: String, what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalMapOfComplex[A, B <: AnyRef](ident: String, what: Option[scala.collection.Map[A,B]], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]

  def addMapOfPrimitives[A, B](ident: String, what: scala.collection.Map[A,B])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalMapOfPrimitives[A, B](ident: String, what: Option[scala.collection.Map[A,B]])(implicit tagA: ClassTag[A], tagB: ClassTag[B]): AlmValidation[WarpSequencer[TDimension]]
    
  /**
   * Dematerialize a Map with keys being of primitive type A and values of type B using a separate decomposer for each value B
   * All elements will be checked individually on how to decompose them.
   * First the element is checked, whether it is a primitive type. If not, the look up mechanism of 'addMapOfComplex' is used
   */
  def addMap[A, B](ident: String, what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalMap[A, B](ident: String, what: Option[scala.collection.Map[A,B]], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]

  /**
   * Dematerialize a Map with keys being of primitive type A and values of type B using a separate decomposer for each value B
   * Items that couldn't be serialized will be skipped
   * All elements will be checked individually on how to decompose them.
   * First the element is checked, whether it is a primitive type. If not, the look up mechanism of 'addMapOfComplex' is used
   */
  def addMapLiberate[A, B](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalMapLiberate[A, B](ident: String, what: Option[scala.collection.Map[A, B]], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]

  def addTreeAllWith[A](ident: String, what: scalaz.Tree[A], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalTreeAllWith[A](ident: String, what: Option[scalaz.Tree[A]], decomposes: Decomposes[A]): AlmValidation[WarpSequencer[TDimension]]
  def addTreeStrict[A <: AnyRef](ident: String, what: scalaz.Tree[A], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalTreeStrict[A <: AnyRef](ident: String, what: Option[scalaz.Tree[A]], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addTreeOfComplex[A <: AnyRef](ident: String, what: scalaz.Tree[A], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalTreeOfComplex[A <: AnyRef](ident: String, what: Option[scalaz.Tree[A]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]
  def addTreeOfPrimitives[A](ident: String, what: scalaz.Tree[A])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalTreeOfPrimitives[A](ident: String, what: Option[scalaz.Tree[A]])(implicit tag: ClassTag[A]): AlmValidation[WarpSequencer[TDimension]]
  def addTree[A](ident: String, what: scalaz.Tree[A], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]
  def addOptionalTree[A](ident: String, what: Option[scalaz.Tree[A]], backupRiftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]
  
  
  def addRiftDescriptor(descriptor: RiftDescriptor): WarpSequencer[TDimension]

  def includeDirect[T <: AnyRef](what: T, decomposer: Decomposer[T]): AlmValidation[WarpSequencer[TDimension]]
  def include(what: AnyRef, riftDescriptor: Option[RiftDescriptor]): AlmValidation[WarpSequencer[TDimension]]
  def include[T <: AnyRef](what: T)(implicit tag: ClassTag[T]): AlmValidation[WarpSequencer[TDimension]]
  def includeWith(f: WarpSequencer[TDimension] => AlmValidation[WarpSequencer[TDimension]]): AlmValidation[WarpSequencer[TDimension]]

  def fail(prob: Problem): AlmValidation[WarpSequencer[TDimension]] = prob.failure
  def ok: AlmValidation[WarpSequencer[TDimension]] = this.success

}



