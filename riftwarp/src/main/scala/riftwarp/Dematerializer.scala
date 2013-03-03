package riftwarp

import scala.reflect.ClassTag
import scala.collection.IterableLike
import almhirt.common._
import riftwarp.components.HasDecomposers

trait Dematerializer[+TDimension <: RiftDimension] {
  type ValueRepr = TDimension#Under

  def getStringRepr(aValue: String): ValueRepr
  def getBooleanRepr(aValue: Boolean): ValueRepr
  def getByteRepr(aValue: Byte): ValueRepr
  def getIntRepr(aValue: Int): ValueRepr
  def getLongRepr(aValue: Long): ValueRepr
  def getBigIntRepr(aValue: BigInt): ValueRepr
  def getFloatRepr(aValue: Float): ValueRepr
  def getDoubleRepr(aValue: Double): ValueRepr
  def getBigDecimalRepr(aValue: BigDecimal): ValueRepr
  def getByteArrayRepr(aValue: Array[Byte]): ValueRepr
  def getBase64EncodedByteArrayRepr(aValue: Array[Byte]): ValueRepr
  def getByteArrayBlobEncodedRepr(aValue: Array[Byte]): ValueRepr
  def getDateTimeRepr(aValue: org.joda.time.DateTime): ValueRepr
  def getUriRepr(aValue: _root_.java.net.URI): ValueRepr
  def getUuidRepr(aValue: _root_.java.util.UUID): ValueRepr
  def getWithRepr[A](ident: String, what: A, decomposes: Decomposes[A])(implicit hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getComplexRepr[A <: AnyRef](ident: String, what: A, backupRiftDescriptor: Option[RiftDescriptor])(implicit hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getComplexByTagRepr[A <: AnyRef](ident: String, what: A)(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getIterableAllWithRepr[A, Coll](ident: String, what: IterableLike[A, Coll], decomposes: Decomposes[A]): AlmValidation[ValueRepr]
  def getIterableStrictRepr[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getIterableOfComplexRepr[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor])(implicit hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getIterableOfPrimitivesRepr[A, Coll](ident: String, what: IterableLike[A, Coll])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getIterableRepr[A, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor])(implicit hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getMapAllWithRepr[A, B](ident: String, what: scala.collection.Map[A,B], decomposes: Decomposes[B])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getMapStrictRepr[A, B <: AnyRef](ident: String, what: scala.collection.Map[A,B], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getMapOfComplexRepr[A, B <: AnyRef](ident: String, what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getMapOfPrimitivesRepr[A, B](ident: String, what: scala.collection.Map[A,B])(implicit tagA: ClassTag[A], tagB: ClassTag[B], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getMapRepr[A, B](ident: String, what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getMapLiberateRepr[A, B](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[ValueRepr]
  def getRiftDescriptorRepr(descriptor: RiftDescriptor): ValueRepr
  
  def getString(aValue: String): TDimension
  def getBoolean(aValue: Boolean): TDimension
  def getByte(aValue: Byte): TDimension
  def getInt(aValue: Int): TDimension
  def getLong(aValue: Long): TDimension
  def getBigInt(aValue: BigInt): TDimension
  def getFloat(aValue: Float): TDimension
  def getDouble(aValue: Double): TDimension
  def getBigDecimal(aValue: BigDecimal): TDimension
  def getByteArray(aValue: Array[Byte]): TDimension
  def getBase64EncodedByteArray(aValue: Array[Byte]): TDimension
  def getByteArrayBlobEncoded(aValue: Array[Byte]): TDimension
  def getDateTime(aValue: org.joda.time.DateTime): TDimension
  def getUri(aValue: _root_.java.net.URI): TDimension
  def getUuid(aValue: _root_.java.util.UUID): TDimension
  def getWith[A](ident: String, what: A, decomposes: Decomposes[A])(implicit hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getComplex[A <: AnyRef](ident: String, what: A, backupRiftDescriptor: Option[RiftDescriptor])(implicit hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getComplexByTag[A <: AnyRef](ident: String, what: A)(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getIterableAllWith[A, Coll](ident: String, what: IterableLike[A, Coll], decomposes: Decomposes[A]): AlmValidation[TDimension]
  def getIterableStrict[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], riftDesc: Option[RiftDescriptor])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getIterableOfComplex[A <: AnyRef, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor])(implicit hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getIterableOfPrimitives[A, Coll](ident: String, what: IterableLike[A, Coll])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getIterable[A, Coll](ident: String, what: IterableLike[A, Coll], backupRiftDescriptor: Option[RiftDescriptor])(implicit hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getMapAllWith[A, B](ident: String, what: scala.collection.Map[A,B], decomposes: Decomposes[B])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getMapStrict[A, B <: AnyRef](ident: String, what: scala.collection.Map[A,B], riftDesc: Option[RiftDescriptor])(implicit tagA: ClassTag[A], tagB: ClassTag[B], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getMapOfComplex[A, B <: AnyRef](ident: String, what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getMapOfPrimitives[A, B](ident: String, what: scala.collection.Map[A,B])(implicit tagA: ClassTag[A], tagB: ClassTag[B], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getMap[A, B](ident: String, what: scala.collection.Map[A,B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getMapLiberate[A, B](ident: String, what: scala.collection.Map[A, B], backupRiftDescriptor: Option[RiftDescriptor])(implicit tag: ClassTag[A], hasDecomposers: HasDecomposers): AlmValidation[TDimension]
  def getRiftDescriptor(descriptor: RiftDescriptor): TDimension
}